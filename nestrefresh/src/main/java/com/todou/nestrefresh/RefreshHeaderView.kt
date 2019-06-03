package com.todou.nestrefresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class RefreshHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr), RefreshHeaderBehavior.RefreshHeaderCallback {

    private lateinit var flipAnimation: RotateAnimation
    private lateinit var reverseFlipAnimation: RotateAnimation

    private lateinit var imageRefreshIndicator: ImageView
    private lateinit var viewProgress: View
    private lateinit var textRefresh: TextView

    private lateinit var textBelowThreshold: CharSequence
    private lateinit var textAboveThreshold: CharSequence
    private lateinit var textRefreshing: CharSequence

    private var belowThreshold = true
    private var state: Int = 0

    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.view_nest_refresh_header, this)

        imageRefreshIndicator = findViewById(R.id.image_refresh)
        viewProgress = findViewById(R.id.progress_loading)
        textRefresh = findViewById(R.id.text_refresh)

        textAboveThreshold = resources.getString(R.string.refresh_release)
        textBelowThreshold = resources.getString(R.string.refresh_pull)
        textRefreshing = resources.getString(R.string.refresh_refreshing)

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

    override fun onScroll(offset: Int, fraction: Float, nextState: Int) {
        if (state != nextState) {
            if (state == RefreshHeaderBehavior.STATE_COLLAPSED) {
                imageRefreshIndicator.clearAnimation()
                imageRefreshIndicator.rotation = 0f
            }
            state = nextState
            if (nextState == RefreshHeaderBehavior.STATE_DRAGGING) {
                if (viewProgress.visibility == View.VISIBLE) {
                    viewProgress.visibility = View.GONE
                }

                if (imageRefreshIndicator.visibility != View.VISIBLE) {
                    imageRefreshIndicator.visibility = View.VISIBLE
                }
                textRefresh.text = if (belowThreshold) textBelowThreshold else textAboveThreshold
            }
        }

        val belowThreshold = fraction < 1
        if (belowThreshold != this.belowThreshold && nextState != RefreshHeaderBehavior.STATE_SETTLING) {
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

    override fun onStateChanged(newState: Int) {
        if (newState == RefreshHeaderBehavior.STATE_HOVERING) {
            textRefresh.text = textRefreshing
            viewProgress.visibility = View.VISIBLE
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.visibility = View.GONE
        }
    }
}
