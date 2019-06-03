package com.todou.nestrefresh

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import com.todou.nestrefresh.base.RefreshHeaderBehavior

import java.lang.ref.WeakReference

class RefreshHoverHeaderBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) :
    RefreshHeaderBehavior<NestRefreshLayout>(context, attrs) {
    private val rectOut = Rect()

    private var lastNestedScrollingChildRef: WeakReference<View>? = null
    private var maxCollapseUp = 0
    override val maxPullRefreshDown = 2000

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: NestRefreshLayout,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        if (lp.height == -2) {
            parent.onMeasureChild(
                child,
                parentWidthMeasureSpec,
                widthUsed,
                View.MeasureSpec.makeMeasureSpec(0, UNSPECIFIED),
                heightUsed
            )
            return true
        } else {
            return super.onMeasureChild(
                parent,
                child,
                parentWidthMeasureSpec,
                widthUsed,
                parentHeightMeasureSpec,
                heightUsed
            )
        }
    }

    override fun layoutChild(parent: CoordinatorLayout, child: NestRefreshLayout, layoutDirection: Int) {
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        val refreshHeaderHeight = child.refreshHeaderHeight
        rectOut.left = lp.leftMargin + parent.paddingLeft
        rectOut.top = lp.topMargin - refreshHeaderHeight + parent.paddingTop
        rectOut.right = rectOut.left + child.measuredWidth
        rectOut.bottom = child.measuredHeight + rectOut.top
        child.layout(rectOut.left, rectOut.top, rectOut.right, rectOut.bottom)
        maxCollapseUp = child.hoverHeight + refreshHeaderHeight - child.height

        setHoveringRange(refreshHeaderHeight)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: NestRefreshLayout,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        val started = axes and 2 != 0
        this.lastNestedScrollingChildRef = null
        return started
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: NestRefreshLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val targetBehavior = getBehavior(target)
        if (targetBehavior != null
            && getBehavior(target) is RefreshHoverScrollBehavior
            && dy != 0
        ) {
            if (dy > 0) {
                val min = maxCollapseUp
                val max = 0
                consumed[1] = this.scroll(coordinatorLayout, child, dy, min, max, type)
                this.stopNestedScrollIfNeeded(dy, child, target, type)
            } else if (dy < 0 && target != null && !target.canScrollVertically(-1)) {
                val min = maxCollapseUp
                val max = 0
                consumed[1] = this.scroll(coordinatorLayout, child, dy, min, max, type)
                this.stopNestedScrollIfNeeded(dy, child, target, type)
            }
        } else if (targetBehavior == null || getBehavior(target) !is RefreshHoverScrollBehavior && dy != 0) {
            if (dy < 0) {//down
            }
            if (dy > 0 && totalSpringOffset > 0) {//up
                val min = maxCollapseUp
                val max = 0
                consumed[1] = this.scroll(coordinatorLayout, child, dy, min, max, type)
                this.stopNestedScrollIfNeeded(dy, child, target, type)
            }
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: NestRefreshLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        this.scroll(coordinatorLayout, child, dyUnconsumed, maxCollapseUp, 0, type)
        this.stopNestedScrollIfNeeded(dyUnconsumed, child, target, type)
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: NestRefreshLayout,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)

        if (!child.childScrollAbleList.contains(target)) {
            this.lastNestedScrollingChildRef = WeakReference(target)
        }
    }

    fun getHoverHeight(layout: NestRefreshLayout): Int {
        return layout.hoverHeight
    }

    override fun getMaxDragOffset(view: NestRefreshLayout): Int {
        return maxCollapseUp
    }

    override fun getScrollRangeForDragFling(view: NestRefreshLayout): Int {
        return -maxCollapseUp
    }


    override fun canDragView(view: NestRefreshLayout): Boolean {
        if (this.lastNestedScrollingChildRef == null) {
            return true
        } else {
            val scrollingView = this.lastNestedScrollingChildRef!!.get()
            return scrollingView != null && scrollingView.isShown && !scrollingView.canScrollVertically(-1)
        }
    }

    override fun childInHeaderCanScroll(view: NestRefreshLayout, x: Float, y: Float): Boolean {
        val viewList = view.childScrollAbleList
        for (i in viewList.indices) {
            val child = viewList[i]
            val location = IntArray(2)
            child.getLocationOnScreen(location)
            val rect = Rect(location[0], location[1], child.width + location[0], child.height + location[1])
            if (rect.contains(x.toInt(), y.toInt())) {
                return true
            }
        }
        return super.childInHeaderCanScroll(view, x, y)
    }

    private fun stopNestedScrollIfNeeded(dy: Int, child: NestRefreshLayout, target: View, type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            val curOffset = this.topBottomOffsetForScrollingSibling
            if (dy < 0 && curOffset >= 0 || dy > 0 && curOffset == getMaxDragOffset(child)) {
                ViewCompat.stopNestedScroll(target, 1)
            }
        }
    }
}
