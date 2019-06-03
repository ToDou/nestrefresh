package com.todou.nestrefresh

import android.content.Context
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.NestedScrollingChild
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.todou.nestrefresh.base.RefreshHeaderBehavior

import java.util.ArrayList

class NestRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr), RefreshCallback, CoordinatorLayout.AttachedBehavior {

    var childScrollAbleList: List<View> = ArrayList()
        private set
    private lateinit var behaviorHover: RefreshHoverHeaderBehavior
    private var onRefreshListener: OnRefreshListener? = null
    private var headerView: View? = null

    val hoverHeight: Int
        get() {
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val layoutParams = view.layoutParams as LayoutParams
                if (layoutParams.scrollFlags and LayoutParams.SCROLL_FLAG_HOVER != 0) {
                    return view.measuredHeight
                }
            }
            return 0
        }

    val refreshHeaderHeight: Int
        get() {
            var height = 0
            if (headerView == null) {
                for (i in 0 until childCount) {
                    val view = getChildAt(i)
                    val layoutParams = view.layoutParams as LayoutParams
                    if (layoutParams.scrollFlags and LayoutParams.SCROLL_FLAG_REFRESH_HEADER != 0) {
                        headerView = view
                        break
                    }
                }
            }
            if (headerView != null) {
                height = headerView!!.measuredHeight
            }
            return height
        }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateAllChildList()
        updateBehaviorInfo()
    }

    private fun updateBehaviorInfo() {
        val lp = layoutParams
        if (lp is CoordinatorLayout.LayoutParams) {
            val behavior = lp.behavior
            if (behavior is RefreshHoverHeaderBehavior) {
                this.behaviorHover = behavior
                behavior.setSpringHeaderCallback(this)
            }
        }
    }

    private fun updateAllChildList() {
        childScrollAbleList = getScrollableChild(this)
    }

    private fun getScrollableChild(viewGroup: ViewGroup): List<View> {
        val result = ArrayList<View>()
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            if (view is NestedScrollingChild) {
                val nestedScrollingChild = view as NestedScrollingChild
                if (nestedScrollingChild.isNestedScrollingEnabled) {
                    result.add(view)
                }
            }
            if (view is ViewGroup) {
                result.addAll(getScrollableChild(view))
            }
        }
        return result
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LinearLayout.LayoutParams {
        return LayoutParams(-1, -2)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LinearLayout.LayoutParams {
        return LayoutParams(this.context, attrs)
    }


    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): LinearLayout.LayoutParams {
        return if (Build.VERSION.SDK_INT >= 19 && lp is LinearLayout.LayoutParams) {
            LayoutParams(lp)
        } else {
            (lp as? MarginLayoutParams)?.let { LayoutParams(it) } ?: LayoutParams(lp)
        }
    }

    override fun onScroll(offset: Int, fraction: Float, nextState: Int) {
        onScrollToRefreshHeader(offset, fraction, nextState)
    }

    override fun onStateChanged(newState: Int) {
        onStateChangedToRefreshHeader(newState)
        if (newState == RefreshHeaderBehavior.STATE_HOVERING) {
            onRefreshListener?.onRefresh()
        }
    }

    private fun onScrollToRefreshHeader(offset: Int, fraction: Float, nextState: Int) {
        if (headerView is RefreshCallback) {
            (headerView as RefreshCallback).onScroll(offset, fraction, nextState)
        }
    }

    private fun onStateChangedToRefreshHeader(newState: Int) {
        if (headerView is RefreshCallback) {
            (headerView as RefreshCallback).onStateChanged(newState)
        }
    }

    fun setRefresh(refreshing: Boolean) {
        behaviorHover.setState(
            if (refreshing)
                RefreshHeaderBehavior.STATE_HOVERING
            else
                RefreshHeaderBehavior.STATE_COLLAPSED
        )
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        behaviorHover = RefreshHoverHeaderBehavior()
        return behaviorHover
    }

    class LayoutParams : LinearLayout.LayoutParams {

        var scrollFlags: Int = 0

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.NestRefreshLayout_Layout)
            this.scrollFlags = a.getInt(R.styleable.NestRefreshLayout_Layout_nest_refresh_layout_scrollFlags, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: MarginLayoutParams) : super(source)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        companion object {
            val SCROLL_FLAG_HOVER = 0x1
            val SCROLL_FLAG_REFRESH_HEADER = 0x2
        }
    }

    interface OnRefreshListener {
        fun onRefresh()
    }
}
