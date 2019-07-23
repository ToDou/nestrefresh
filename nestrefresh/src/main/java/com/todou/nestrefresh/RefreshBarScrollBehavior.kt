package com.todou.nestrefresh

import android.content.Context
import android.graphics.Rect
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.todou.nestrefresh.base.BaseBehavior

class RefreshBarScrollBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) :
    BaseBehavior<View>(context, attrs) {

    private val rectOut = Rect()
    private var refreshBarBehavior: RefreshBarBehavior? = null
    private var loadMoreBehavior: LoadMoreBehavior? = null

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        val childLpHeight = child.layoutParams.height
        if (childLpHeight == ViewGroup.LayoutParams.WRAP_CONTENT || childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            val dependencies = parent.getDependencies(child)
            val header = findFirstDependency(dependencies)
            header?.let {
                var availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec)
                if (availableHeight == 0) {
                    availableHeight = parent.height
                }

                val height = availableHeight - getPinHeightWithoutInset(it)
                val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    height, if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT)
                        View.MeasureSpec.EXACTLY
                    else
                        View.MeasureSpec.AT_MOST
                )
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed)
                return true
            }
        }
        return false
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val behavior = getBehavior(dependency) ?: return false
        return behavior is RefreshBarBehavior
                || behavior is LoadMoreBehavior
                || behavior is RefreshBehavior
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        val dependencies = parent.getDependencies(child)
        val header = this.findFirstDependency(dependencies)
        header?.let {
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            rectOut.left = lp.leftMargin + parent.paddingLeft
            rectOut.right = rectOut.left + child.measuredWidth
            rectOut.top = lp.topMargin + it.bottom - getHeaderOffset(it)
            rectOut.bottom = rectOut.top + child.measuredHeight
            child.layout(rectOut.left, rectOut.top, rectOut.right, rectOut.bottom)
        } ?: run {
            super.layoutChild(parent, child, layoutDirection)
        }
    }

    private fun getHeaderOffset(header: View): Int {
        if (header.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = header.layoutParams as CoordinatorLayout.LayoutParams
            if (params.behavior is RefreshBarBehavior) {
                return (params.behavior as RefreshBarBehavior).getTopAndBottomOffset()
            }
        }
        return 0
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val behavior = getBehavior(dependency)
        var offset = 0
        if (behavior is RefreshBarBehavior) {
            refreshBarBehavior = behavior
            offset = behavior.getTopAndBottomOffset()
        } else if (behavior is LoadMoreBehavior) {
            loadMoreBehavior = behavior
            offset = behavior.currentRange() + getHeaderOffsetByBehavior()
        }
        return setTopAndBottomOffset(offset)
    }

    private fun getHeaderOffsetByBehavior(): Int {
        return refreshBarBehavior?.getTopAndBottomOffset() ?: 0
    }

    private fun findFirstDependency(dependencies: List<View>): View? {
        for (view in dependencies) {
            val layoutParams = view.layoutParams
            if (layoutParams is CoordinatorLayout.LayoutParams) {
                if (layoutParams.behavior is RefreshBarBehavior) {
                    return view
                }
            }
        }
        return null
    }

    private fun getPinHeightWithoutInset(view: View): Int {
        return if (view is RefreshBarLayout) {
            view.getPinHeightWithoutInsetTop()
        } else {
            0
        }
    }
}
