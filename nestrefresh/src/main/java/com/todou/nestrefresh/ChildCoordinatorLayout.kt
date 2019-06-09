package com.todou.nestrefresh

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.NestedScrollingChild2
import android.support.v4.view.NestedScrollingChildHelper
import android.util.AttributeSet
import android.view.View

class ChildCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), NestedScrollingChild2 {

    private lateinit var mChildHelper: NestedScrollingChildHelper

    init {
        init()
    }

    private fun init() {
        mChildHelper = NestedScrollingChildHelper(this)
        mChildHelper.isNestedScrollingEnabled = true
    }


    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(i: Int, i1: Int): Boolean {
        return mChildHelper.startNestedScroll(i, i1)
    }

    override fun stopNestedScroll(i: Int) {
        mChildHelper.stopNestedScroll(i)
    }

    override fun hasNestedScrollingParent(i: Int): Boolean {
        return mChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(i: Int, i1: Int, i2: Int, i3: Int, ints: IntArray?, i4: Int): Boolean {
        return mChildHelper.dispatchNestedScroll(i, i1, i2, i3, ints, i4)
    }

    override fun dispatchNestedPreScroll(i: Int, i1: Int, ints: IntArray?, ints1: IntArray?, i2: Int): Boolean {
        return mChildHelper.dispatchNestedPreScroll(i, i1, ints, ints1, i2)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        val handle = super.onStartNestedScroll(child, target, axes, type)
        return startNestedScroll(axes, type) || handle
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        super.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val thisConsume = IntArray(2)
        dispatchNestedPreScroll(dx, dy, thisConsume, null, type)
        consumed[0] += thisConsume[0]
        consumed[1] += thisConsume[1]
        thisConsume[0] = 0
        thisConsume[1] = 0
        super.onNestedPreScroll(target, dx - consumed[0], dy - consumed[1], thisConsume, type)
        consumed[0] += thisConsume[0]
        consumed[1] += thisConsume[1]
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        val handled = super.onNestedPreFling(target, velocityX, velocityY)
        return dispatchNestedPreFling(velocityX, velocityY) || handled
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        val handled = super.onNestedFling(target, velocityX, velocityY, consumed)
        return dispatchNestedFling(velocityX, velocityY, consumed) || handled
    }

}
