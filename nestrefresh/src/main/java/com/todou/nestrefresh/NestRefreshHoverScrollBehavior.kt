package com.todou.nestrefresh

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class NestRefreshHoverScrollBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) : BaseBehavior<View>(context, attrs) {

    private val rectOut = Rect()

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
            if (header != null) {
                var availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec)
                if (availableHeight == 0) {
                    availableHeight = parent.height
                }

                val height = availableHeight - getHoverHeight(header)
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
        val behavior = getBehavior(dependency)
        return behavior != null && behavior is NestRefreshHoverHeaderBehavior
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        val dependencies = parent.getDependencies(child)
        val header = this.findFirstDependency(dependencies)
        if (header != null) {
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            rectOut.left = lp.leftMargin + parent.paddingLeft
            rectOut.right = rectOut.left + child.measuredWidth
            rectOut.top = lp.topMargin + header.bottom - getHeaderOffset(header)
            rectOut.bottom = rectOut.top + child.measuredHeight
            child.layout(rectOut.left, rectOut.top, rectOut.right, rectOut.bottom)
        }
    }

    private fun getHeaderOffset(header: View): Int {
        if (header.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = header.layoutParams as CoordinatorLayout.LayoutParams
            if (params.behavior is NestRefreshHoverHeaderBehavior) {
                return (params.behavior as NestRefreshHoverHeaderBehavior).getTopAndBottomOffset()
            }
        }
        return 0
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val offset = (getBehavior(dependency) as NestRefreshHoverHeaderBehavior).getTopAndBottomOffset()
        return setTopAndBottomOffset(offset)
    }

    private fun findFirstDependency(dependencies: List<View>): View? {
        for (view in dependencies) {
            val layoutParams = view.layoutParams
            if (layoutParams is CoordinatorLayout.LayoutParams) {
                if (layoutParams.behavior is NestRefreshHoverHeaderBehavior) {
                    return view
                }
            }
        }
        return null
    }

    private fun getHoverHeight(view: View): Int {
        val behavior = getBehavior(view)
        return (behavior as? NestRefreshHoverHeaderBehavior)?.getHoverHeight(view as NestRefreshLayout) ?: 0
    }
}
