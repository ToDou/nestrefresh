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

    private var mState = STATE_COLLAPSED

    private var mCallback: LoadMoreFooterCallback? = null

    var totalUnconsumed: Float = 0.toFloat()
        private set
    private val mRectOut = Rect()

    private var mHoveringRange = UNSET
    private var mMaxRange = UNSET
    private var mHoveringOffset: Int = 0

    private var mAnimator: ValueAnimator? = null
    private var mEndListener: EndListener? = null
    private var mHasMore = true

    //TODo test
    private var mShowFooterEnable = true

    private val stateByHasMoreWhenHover: Int
        get() = if (mHasMore) {
            STATE_HOVERING
        } else {
            STATE_COLLAPSED
        }

    val currentRange: Int
        get() = getTopAndBottomOffset()

    fun setMaxRange(maxRange: Int) {
        mMaxRange = maxRange
    }

    fun setHasMore(hasMore: Boolean) {
        this.mHasMore = hasMore
    }

    fun setHoveringRange(hoveringRange: Int) {
        mHoveringRange = hoveringRange
        mHoveringOffset = -mHoveringRange
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        val parentHeight = parent.height

        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        mRectOut.left = lp.leftMargin + parent.paddingLeft
        mRectOut.right = mRectOut.left + child.measuredWidth
        mRectOut.top = lp.topMargin + parent.bottom
        mRectOut.bottom = mRectOut.top + child.measuredHeight
        child.layout(mRectOut.left, mRectOut.top, mRectOut.right, mRectOut.bottom)

        val childHeight = child.height

        if (mCallback != null) mCallback!!.updateChildHeight(childHeight)

        if (mHoveringRange == UNSET) {
            setHoveringRange(childHeight)
        }

        if (mMaxRange == UNSET) {
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
        val started = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        if (started && mAnimator != null && mAnimator!!.isRunning) {
            mAnimator!!.cancel()
        }
        return started && mShowFooterEnable
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
        animateOffsetToState(if (getTopAndBottomOffset() <= mHoveringOffset) stateByHasMoreWhenHover else STATE_COLLAPSED)
    }

    private fun animateOffsetToState(endState: Int) {
        val from = getTopAndBottomOffset()
        val to = if (endState == STATE_HOVERING) mHoveringOffset else 0
        if (from == to) {
            setStateInternal(endState)
            return
        } else {
            setStateInternal(STATE_SETTLING)
        }

        if (mAnimator == null) {
            mAnimator = ValueAnimator()
            mAnimator!!.duration = 200
            mAnimator!!.interpolator = DecelerateInterpolator()
            mAnimator!!.addUpdateListener { animation -> setTopAndBottomOffset(animation.animatedValue as Int) }
            mEndListener = EndListener(endState)
            mAnimator!!.addListener(mEndListener)
        } else {
            if (mAnimator!!.isRunning) {
                mAnimator!!.cancel()
            }
            mEndListener!!.setEndState(endState)
        }
        mAnimator!!.setIntValues(from, to)
        mAnimator!!.start()
    }

    override fun setTopAndBottomOffset(offset: Int): Boolean {
        if (mCallback != null) {
            mCallback!!.onScroll(offset, offset.toFloat() / mHoveringRange, mState, mHasMore)
        }
        return super.setTopAndBottomOffset(offset)
    }

    protected fun resetTopAndBottomOffset(offset: Int) {
        super.setTopAndBottomOffset(offset)
    }

    private fun setStateInternal(state: Int) {
        if (state == mState) {
            return
        }
        mState = state
        if (mCallback != null) {
            mCallback!!.onStateChanged(state, mHasMore)
        }
    }

    fun setState(state: Int) {
        var state = state
        if (state != STATE_COLLAPSED && state != STATE_HOVERING) {
            throw IllegalArgumentException("Illegal state argument: $state")
        } else if (state != mState) {
            if (state == STATE_HOVERING) {
                state = stateByHasMoreWhenHover
            }
            animateOffsetToState(state)
        }
    }

    private fun calculateScrollOffset(): Int {
        return (-mMaxRange * (1 - Math.exp((totalUnconsumed / mMaxRange.toFloat() / 2f).toDouble()))).toInt()
    }

    private fun calculateScrollUnconsumed(): Int {
        return (-Math.log((1 - currentRange.toFloat() / mMaxRange).toDouble()) * mMaxRange.toDouble() * 2.0).toInt()
    }

    fun setFooterCallback(callback: LoadMoreFooterCallback) {
        mCallback = callback
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

    fun setShowFooterEnable(showFooterEnable: Boolean) {
        mShowFooterEnable = showFooterEnable
    }

    companion object {

        private const val UNSET = Integer.MIN_VALUE

        const val STATE_COLLAPSED = 1
        const val STATE_HOVERING = 2
        const val STATE_DRAGGING = 3
        const val STATE_SETTLING = 4
    }
}
