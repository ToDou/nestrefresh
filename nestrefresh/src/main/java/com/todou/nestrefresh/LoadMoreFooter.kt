package com.todou.nestrefresh

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.todou.nestrefresh.base.LoadMoreFooterCallback

import com.todou.nestrefresh.LoadMoreFooterBehavior.Companion.STATE_COLLAPSED
import com.todou.nestrefresh.LoadMoreFooterBehavior.Companion.STATE_HOVERING
import com.todou.nestrefresh.LoadMoreFooterBehavior.Companion.STATE_DRAGGING
import com.todou.nestrefresh.LoadMoreFooterBehavior.Companion.STATE_SETTLING

class LoadMoreFooter @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr), LoadMoreFooterCallback {

    private lateinit var flipAnimation: RotateAnimation
    private lateinit var reverseFlipAnimation: RotateAnimation

    private lateinit var imageRefreshIndicator: ImageView
    private lateinit var viewProgress: View
    private lateinit var textRefresh: TextView

    private lateinit var textBelowThreshold: CharSequence
    private lateinit var textAboveThreshold: CharSequence
    private lateinit var textRefreshing: CharSequence
    private lateinit var textNoMore: CharSequence

    private var behavior: LoadMoreFooterBehavior? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null

    private var belowThreshold = true
    private var state: Int = 0

    init {
        init(context, attrs, defStyleAttr)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val lp = layoutParams
        if (lp is CoordinatorLayout.LayoutParams) {
            val behavior = lp.behavior
            if (behavior is LoadMoreFooterBehavior) {
                behavior.setFooterCallback(this)
                behavior.setShowFooterEnable(isEnabled)
                this.behavior = behavior
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        behavior?.setShowFooterEnable(enabled)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.view_nest_load_more_footer, this)

        imageRefreshIndicator = findViewById(R.id.image_refresh)
        viewProgress = findViewById(R.id.progress_loading)
        textRefresh = findViewById(R.id.text_refresh)

        textAboveThreshold = resources.getString(R.string.nest_refresh_load_more_pull_label)
        textBelowThreshold = resources.getString(R.string.nest_refresh_load_more_release_label)
        textRefreshing = resources.getString(R.string.nest_refresh_load_more_refreshing_label)
        textNoMore = resources.getString(R.string.nest_refresh_load_more_no_more)

        initAnimation()
    }

    private fun initAnimation() {
        flipAnimation = RotateAnimation(
            0f, -180f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        flipAnimation.interpolator = LinearInterpolator()
        flipAnimation.duration = 250
        flipAnimation.fillAfter = true
        reverseFlipAnimation = RotateAnimation(
            -180f, -360f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        reverseFlipAnimation.interpolator = LinearInterpolator()
        reverseFlipAnimation.duration = 250
        reverseFlipAnimation.fillAfter = true
    }

    override fun onScroll(offset: Int, fraction: Float, nextState: Int, hasMore: Boolean) {
        if (hasMore) {
            doHasMoreScroll(offset, fraction, nextState)
        } else {
            doNoMoreScroll(offset, fraction, nextState)
        }
    }

    private fun doNoMoreScroll(offset: Int, fraction: Float, nextState: Int) {
        if (state != nextState) {
            state = nextState
            textRefresh.text = textNoMore
            viewProgress.visibility = View.GONE
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.visibility = View.GONE
        }
    }

    private fun doHasMoreScroll(offset: Int, fraction: Float, nextState: Int) {
        if (state != nextState) {
            if (state == STATE_COLLAPSED) {
                imageRefreshIndicator.clearAnimation()
                imageRefreshIndicator.rotation = 0f
            }
            state = nextState
            if (nextState == STATE_DRAGGING) {
                if (viewProgress.visibility == View.VISIBLE) {
                    viewProgress.visibility = View.GONE
                }

                if (imageRefreshIndicator.visibility != View.VISIBLE) {
                    imageRefreshIndicator.visibility = View.VISIBLE
                }
                textRefresh.text = if (belowThreshold) textBelowThreshold else textAboveThreshold
            }
        }

        val belowThreshold = fraction < -1
        if (belowThreshold != this.belowThreshold && nextState != STATE_SETTLING) {
            this.belowThreshold = belowThreshold
            updateTextAndImage()
        }
    }

    private fun updateTextAndImage() {
        if (belowThreshold) {
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.startAnimation(reverseFlipAnimation)
        } else {
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.startAnimation(flipAnimation)
        }
        textRefresh.text = if (belowThreshold) textBelowThreshold else textAboveThreshold
    }

    override fun onStateChanged(newState: Int, hasMore: Boolean) {
        if (hasMore) {
            if (newState == STATE_HOVERING) {
                textRefresh.text = textRefreshing
                viewProgress.visibility = View.VISIBLE
                imageRefreshIndicator.clearAnimation()
                imageRefreshIndicator.visibility = View.GONE
            }
            if (newState == STATE_HOVERING) {
                onLoadMoreListener?.onLoadMore()
            }
        } else {
            textRefresh.text = textNoMore
            viewProgress.visibility = View.GONE
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.visibility = View.GONE
        }
    }

    fun setIsLoadMore(isLoadMore: Boolean) {
        behavior?.setState(
            if (isLoadMore)
                STATE_HOVERING
            else
                STATE_COLLAPSED
        )
    }

    fun setHasMore(hasMore: Boolean) {
        behavior?.setHasMore(hasMore)
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        onLoadMoreListener = listener
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    override fun updateChildHeight(height: Int) {

    }
}
