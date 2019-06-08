package com.todou.nestrefresh

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet

import android.view.View
import android.view.animation.DecelerateInterpolator

import com.todou.nestrefresh.base.BaseBehavior

class RefreshBehavior(context: Context, attrs: AttributeSet) : BaseBehavior<View>(context, attrs) {

    private var state = STATE_COLLAPSED

    private var callback: RefreshCallback? = null

    private var totalUnconsumed: Float = 0.toFloat()

    private var originalOffset = UNSET
    private var hoveringRange = UNSET
    private var maxRange = UNSET
    private var hoveringOffset: Int = 0

    private var originalOffsetSet: Boolean = false

    private lateinit var animator: ValueAnimator
    private var endListener: EndListener? = null
    private var refreshEnable = true

    val currentRange: Int
        get() = getTopAndBottomOffset() - originalOffset

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.RefreshBehavior_Params)
        setOriginalOffset(
            a.getDimensionPixelSize(
                R.styleable.RefreshBehavior_Params_behavior_originalOffset, UNSET
            )
        )
        setHoveringRange(
            a.getDimensionPixelSize(
                R.styleable.RefreshBehavior_Params_behavior_hoveringRange, UNSET
            )
        )
        setMaxRange(
            a.getDimensionPixelSize(
                R.styleable.RefreshBehavior_Params_behavior_maxRange, UNSET
            )
        )
        a.recycle()
    }

    fun setOriginalOffset(originalOffset: Int) {
        this.originalOffset = originalOffset
    }

    fun setHoveringRange(hoveringRange: Int) {
        this.hoveringRange = hoveringRange
        hoveringOffset = originalOffset + this.hoveringRange
    }

    fun setMaxRange(maxRange: Int) {
        this.maxRange = maxRange
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val handled = super.onLayoutChild(parent, child, layoutDirection)

        val parentHeight = parent.height
        val childHeight = child.height

        if (originalOffset == UNSET) {
            setOriginalOffset(-childHeight)
        }
        if (hoveringRange == UNSET) {
            setHoveringRange(childHeight)
        }
        if (maxRange == UNSET) {
            setMaxRange(parentHeight)
        }

        if (!originalOffsetSet) {
            super.setTopAndBottomOffset(originalOffset)
            originalOffsetSet = true
        }

        return handled
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        val started = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 && state != STATE_HOVERING
        if (started && this::animator.isInitialized && animator.isRunning) {
            animator.cancel()
        }
        return started && refreshEnable
    }

    override fun onNestedScrollAccepted(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ) {
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
        if (dy > 0 && totalUnconsumed > 0) {
            if (dy > totalUnconsumed) {
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
        if (dyUnconsumed < 0) {
            totalUnconsumed -= dyUnconsumed.toFloat()
            setTopAndBottomOffset(calculateScrollOffset())
            setStateInternal(STATE_DRAGGING)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        animateOffsetToState(
            if (getTopAndBottomOffset() >= hoveringOffset)
                STATE_HOVERING
            else
                STATE_COLLAPSED
        )
    }

    private fun animateOffsetToState(endState: Int) {
        val from = getTopAndBottomOffset()
        val to = if (endState == STATE_HOVERING) hoveringOffset else originalOffset
        if (from == to) {
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
        callback?.onScroll(offset, (offset - originalOffset).toFloat() / hoveringRange, state)
        return super.setTopAndBottomOffset(offset)
    }

    private fun setStateInternal(state: Int) {
        if (state == this.state) {
            return
        }
        this.state = state
        callback?.onStateChanged(state)
    }

    fun setState(state: Int) {
        if (state != STATE_COLLAPSED && state != STATE_HOVERING) {
            throw IllegalArgumentException("Illegal state argument: $state")
        } else if (state != this.state) {
            animateOffsetToState(state)
        }
    }

    private fun calculateScrollOffset(): Int {
        return (maxRange * (1 - Math.exp((-(totalUnconsumed / maxRange.toFloat() / 2f)).toDouble()))).toInt() + originalOffset
    }

    private fun calculateScrollUnconsumed(): Int {
        return (-Math.log((1 - currentRange.toFloat() / maxRange).toDouble()) * maxRange.toDouble() * 2.0).toInt()
    }

    fun setRefreshCallback(callback: RefreshCallback) {
        this.callback = callback
    }

    private inner class EndListener(private var mEndState: Int) : AnimatorListenerAdapter() {
        private var mCanceling: Boolean = false

        fun setEndState(finalState: Int) {
            mEndState = finalState
        }

        override fun onAnimationStart(animation: Animator) {
            mCanceling = false
        }

        override fun onAnimationCancel(animation: Animator) {
            mCanceling = true
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!mCanceling) {
                setStateInternal(mEndState)
            }
        }
    }

    fun setRefreshEnable(refreshEnable: Boolean) {
        this.refreshEnable = refreshEnable
    }

    companion object {

        private const val UNSET = Integer.MIN_VALUE

        const val STATE_COLLAPSED = 1
        const val STATE_HOVERING = 2
        const val STATE_DRAGGING = 3
        const val STATE_SETTLING = 4
    }
}
