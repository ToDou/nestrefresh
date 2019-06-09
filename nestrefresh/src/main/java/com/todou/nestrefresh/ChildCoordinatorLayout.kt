package com.todou.nestrefresh

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.NestedScrollingChild2
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewCompat.TYPE_NON_TOUCH
import android.support.v4.view.ViewCompat.TYPE_TOUCH
import android.util.AttributeSet
import android.util.Log
import android.view.View

class ChildCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), NestedScrollingChild2 {

    private lateinit var childHelper: NestedScrollingChildHelper

    init {
        init()
    }

    private fun init() {
        childHelper = NestedScrollingChildHelper(this)
        childHelper.isNestedScrollingEnabled = true
    }


    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(i: Int, i1: Int): Boolean {
        return childHelper.startNestedScroll(i, i1)
    }

    override fun stopNestedScroll(i: Int) {
        childHelper.stopNestedScroll(i)
    }

    override fun hasNestedScrollingParent(i: Int): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(i: Int, i1: Int, i2: Int, i3: Int, ints: IntArray?, i4: Int): Boolean {
        return childHelper.dispatchNestedScroll(i, i1, i2, i3, ints, i4)
    }

    override fun dispatchNestedPreScroll(i: Int, i1: Int, ints: IntArray?, ints1: IntArray?, i2: Int): Boolean {
        return childHelper.dispatchNestedPreScroll(i, i1, ints, ints1, i2)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if (type == TYPE_TOUCH) {
            stopNestedScroll(TYPE_NON_TOUCH)
            ViewCompat.stopNestedScroll(child, TYPE_NON_TOUCH)
        }
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
