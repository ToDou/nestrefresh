package com.todou.nestrefresh

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.todou.nestrefresh.base.BaseBehavior
import com.todou.nestrefresh.base.LoadMoreFooterCallback

class LoadMoreFooterBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) :
    BaseBehavior<View>(context, attrs) {

    private var state = STATE_COLLAPSED

    private var callback: LoadMoreFooterCallback? = null

    var totalUnconsumed: Float = 0.toFloat()
        private set
    private val rectOut = Rect()

    private var hoveringRange = UNSET
    private var maxRange = UNSET
    private var hoveringOffset: Int = 0

    private lateinit var animator: ValueAnimator
    private var endListener: EndListener? = null
    private var hasMore = true
    private var isOnTouch = false

    private var showFooterEnable = false

    private val stateByHasMoreWhenHover: Int
        get() = if (hasMore) {
            STATE_HOVERING
        } else {
            STATE_COLLAPSED
        }

    val currentRange: Int
        get() = getTopAndBottomOffset()

    fun setMaxRange(maxRange: Int) {
        this.maxRange = maxRange
    }

    fun setHasMore(hasMore: Boolean) {
        this.hasMore = hasMore
    }

    fun setHoveringRange(hoveringRange: Int) {
        this.hoveringRange = hoveringRange
        hoveringOffset = -this.hoveringRange
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        val parentHeight = parent.height

        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        rectOut.left = lp.leftMargin + parent.paddingLeft
        rectOut.right = rectOut.left + child.measuredWidth
        rectOut.top = lp.topMargin + parent.bottom
        rectOut.bottom = rectOut.top + child.measuredHeight
        child.layout(rectOut.left, rectOut.top, rectOut.right, rectOut.bottom)

        val childHeight = child.height

        callback?.updateChildHeight(childHeight)

        if (hoveringRange == UNSET) {
            setHoveringRange(childHeight + lp.topMargin + lp.bottomMargin)
        }

        if (maxRange == UNSET) {
            setMaxRange(parentHeight)
        }
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        isOnTouch = true
        val started = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        if (started && this::animator.isInitialized && animator.isRunning) {
            animator.cancel()
        }
        return started && showFooterEnable
    }

    override fun onNestedScrollAccepted(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type)
        totalUnconsumed = calculateScrollUnconsumed().toFloat()
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        if (getBehavior(target) !is RefreshHoverScrollBehavior) {
            return
        }
        if (dy < 0 && totalUnconsumed < 0) {
            if (dy < totalUnconsumed) {
                consumed[1] = dy - totalUnconsumed.toInt()
                totalUnconsumed = 0f
            } else {
                totalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            setTopAndBottomOffset(calculateScrollOffset())
            setStateInternal(STATE_DRAGGING)
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        if (getBehavior(target) !is RefreshHoverScrollBehavior) {
            return
        }
        if (dyUnconsumed > 0) {
            totalUnconsumed -= dyUnconsumed.toFloat()
            setTopAndBottomOffset(calculateScrollOffset())
            setStateInternal(STATE_DRAGGING)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        isOnTouch = false
        animateOffsetToState(if (getTopAndBottomOffset() <= hoveringOffset) stateByHasMoreWhenHover else STATE_COLLAPSED)
    }

    private fun animateOffsetToState(endState: Int) {
        val from = getTopAndBottomOffset()
        val to = if (endState == STATE_HOVERING) hoveringOffset else 0
        if (from == to || isOnTouch) {
            setStateInternal(endState)
            return
        } else {
            setStateInternal(STATE_SETTLING)
        }

        if (!this::animator.isInitialized) {
            animator = ValueAnimator()
            animator.duration = 200
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation -> setTopAndBottomOffset(animation.animatedValue as Int) }
            endListener = EndListener(endState)
            animator.addListener(endListener)
        } else {
            if (animator.isRunning) {
                animator.cancel()
            }
            endListener?.setEndState(endState)
        }
        animator.setIntValues(from, to)
        animator.start()
    }

    override fun setTopAndBottomOffset(offset: Int): Boolean {
        callback?.onScroll(offset, offset.toFloat() / hoveringRange, state, hasMore)
        return super.setTopAndBottomOffset(offset)
    }

    protected fun resetTopAndBottomOffset(offset: Int) {
        super.setTopAndBottomOffset(offset)
    }

    private fun setStateInternal(state: Int) {
        if (state == this.state) {
            return
        }
        this.state = state
        callback?.onStateChanged(state, hasMore)
    }

    fun setState(state: Int) {
        var state = state
        if (state != STATE_COLLAPSED && state != STATE_HOVERING) {
            throw IllegalArgumentException("Illegal state argument: $state")
        } else if (state != this.state) {
            if (state == STATE_HOVERING) {
                state = stateByHasMoreWhenHover
            }
            animateOffsetToState(state)
        }
    }

    private fun calculateScrollOffset(): Int {
        return (-maxRange * (1 - Math.exp((totalUnconsumed / maxRange.toFloat() / 2f).toDouble()))).toInt()
    }

    private fun calculateScrollUnconsumed(): Int {
        return (-Math.log((1 - currentRange.toFloat() / maxRange).toDouble()) * maxRange.toDouble() * 2.0).toInt()
    }

    fun setFooterCallback(callback: LoadMoreFooterCallback) {
        this.callback = callback
    }

    private inner class EndListener(private var endState: Int) : AnimatorListenerAdapter() {
        private var canceling: Boolean = false

        fun setEndState(finalState: Int) {
            endState = finalState
        }

        override fun onAnimationStart(animation: Animator) {
            canceling = false
        }

        override fun onAnimationCancel(animation: Animator) {
            canceling = true
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!canceling) {
                setStateInternal(endState)
            }
        }
    }

    fun setShowFooterEnable(showFooterEnable: Boolean) {
        this.showFooterEnable = showFooterEnable
    }

    companion object {

        const val UNSET = Integer.MIN_VALUE

        const val STATE_COLLAPSED = 1
        const val STATE_HOVERING = 2
        const val STATE_DRAGGING = 3
        const val STATE_SETTLING = 4
    }
}
