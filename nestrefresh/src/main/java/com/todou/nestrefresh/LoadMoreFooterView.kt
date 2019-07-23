package com.todou.nestrefresh

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import com.todou.nestrefresh.base.LoadMoreFooterCallback

import com.todou.nestrefresh.base.LoadMoreFooter
import com.todou.nestrefresh.base.OnLoadMoreListener
import com.todou.nestrefresh.base.State.STATE_HOVERING
import kotlinx.android.synthetic.main.view_nest_load_more_footer.view.*

class LoadMoreFooterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr), LoadMoreFooterCallback, CoordinatorLayout.AttachedBehavior {

    private lateinit var flipAnimation: RotateAnimation
    private lateinit var reverseFlipAnimation: RotateAnimation

    private lateinit var textBelowThreshold: CharSequence
    private lateinit var textAboveThreshold: CharSequence
    private lateinit var text_refreshing: CharSequence
    private lateinit var textNoMore: CharSequence

    private var loadMoreFooter: LoadMoreFooter? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null

    private var belowThreshold = true
    private var state: Int = 0

    private var isLoadMoreIng = false

    init {
        init(context, attrs, defStyleAttr)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        loadMoreFooter?.setShowFooterEnable(enabled)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.view_nest_load_more_footer, this)

        textAboveThreshold = resources.getString(R.string.nest_refresh_load_more_pull_label)
        textBelowThreshold = resources.getString(R.string.nest_refresh_load_more_release_label)
        text_refreshing = resources.getString(R.string.nest_refresh_load_more_refreshing_label)
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

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        val behavior = LoadMoreBehavior()
        this.loadMoreFooter = behavior
        loadMoreFooter?.setFooterCallback(this)
        loadMoreFooter?.setShowFooterEnable(isEnabled)
        return behavior
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
            text_refresh.text = textNoMore
            progress_loading.visibility = View.GONE
            image_refresh.clearAnimation()
            image_refresh.visibility = View.GONE
        }
    }

    private fun doHasMoreScroll(offset: Int, fraction: Float, nextState: Int) {
        val belowThreshold = fraction < -1
        if (!isLoadMoreIng && belowThreshold != this.belowThreshold) {
            this.belowThreshold = belowThreshold
            updateTextAndImage()
        }
    }

    private fun updateTextAndImage() {
        progress_loading.visibility = View.GONE
        if (belowThreshold) {
            image_refresh.clearAnimation()
            image_refresh.startAnimation(reverseFlipAnimation)
        } else {
            image_refresh.clearAnimation()
            image_refresh.startAnimation(flipAnimation)
        }
        text_refresh.text = if (belowThreshold) textBelowThreshold else textAboveThreshold
    }

    override fun onStateChanged(newState: Int, hasMore: Boolean) {
        if (hasMore) {
            if (!isLoadMoreIng && newState == STATE_HOVERING) {
                text_refresh.text = text_refreshing
                progress_loading.visibility = View.VISIBLE
                image_refresh.clearAnimation()
                image_refresh.visibility = View.GONE
            }
            if (!isLoadMoreIng && newState == STATE_HOVERING) {
                onLoadMoreListener?.onLoadMore()
                isLoadMoreIng = true
            }
        } else {
            text_refresh.text = textNoMore
            progress_loading.visibility = View.GONE
            image_refresh.clearAnimation()
            image_refresh.visibility = View.GONE
        }
    }

    fun stopLoadMore() {
        isLoadMoreIng = false
        loadMoreFooter?.stopLoadMore()
        progress_loading.visibility = View.GONE
        image_refresh.visibility = View.VISIBLE
    }

    fun setHasMore(hasMore: Boolean) {
        loadMoreFooter?.setHasMore(hasMore)
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        onLoadMoreListener = listener
    }

    override fun updateChildHeight(height: Int) {

    }
}
