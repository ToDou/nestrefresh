package com.todou.nestrefresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.base.RefreshCallback
import android.support.design.widget.CoordinatorLayout
import android.support.v4.util.ObjectsCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat
import com.todou.nestrefresh.base.RefreshHeader
import com.todou.nestrefresh.base.State.STATE_HOVERING

class RefreshHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), RefreshCallback {

    private lateinit var flipAnimation: RotateAnimation
    private lateinit var reverseFlipAnimation: RotateAnimation

    private var lastInsets: WindowInsetsCompat? = null

    private lateinit var imageRefreshIndicator: ImageView
    private lateinit var viewProgress: View
    private lateinit var textRefresh: TextView

    private lateinit var textBelowThreshold: CharSequence
    private lateinit var textAboveThreshold: CharSequence
    private lateinit var textRefreshing: CharSequence

    private var belowThreshold = true
    private var onRefreshListener: OnRefreshListener? = null
    private var refreshHeader: RefreshHeader? = null

    private var isRefreshing = false

    init {
        init(context, attrs, defStyleAttr)

        ViewCompat.setOnApplyWindowInsetsListener(
            this
        ) { _, windowInsetsCompat -> onWindowInsetChanged(windowInsetsCompat) }
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.view_nest_refresh_header, this)

        imageRefreshIndicator = findViewById(R.id.image_refresh)
        viewProgress = findViewById(R.id.progress_loading)
        textRefresh = findViewById(R.id.text_refresh)

        textAboveThreshold = resources.getString(R.string.nest_refresh_release)
        textBelowThreshold = resources.getString(R.string.nest_refresh_pull)
        textRefreshing = resources.getString(R.string.nest_refresh_refreshing)

        initAnimation()
    }

    private fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null

        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets
        }

        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets
            if (layoutParams is MarginLayoutParams) {
                val marginLayoutParams = layoutParams as MarginLayoutParams
                if (marginLayoutParams.topMargin == 0) {
                    marginLayoutParams.topMargin = lastInsets?.systemWindowInsetTop ?: 0
                }
            }
            requestLayout()
        }
        return insets
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
        val belowThreshold = fraction > 1
        if (!isRefreshing && belowThreshold != this.belowThreshold) {
            this.belowThreshold = belowThreshold
            updateTextAndImage()
        }
    }

    private fun updateTextAndImage() {
        viewProgress.visibility = View.GONE
        if (!belowThreshold) {
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.startAnimation(reverseFlipAnimation)
        } else {
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.startAnimation(flipAnimation)
        }
        textRefresh.text = if (!belowThreshold) textBelowThreshold else textAboveThreshold
    }

    override fun onStateChanged(newState: Int) {
        if (!isRefreshing && newState == STATE_HOVERING) {
            textRefresh.text = textRefreshing
            viewProgress.visibility = View.VISIBLE
            imageRefreshIndicator.clearAnimation()
            imageRefreshIndicator.visibility = View.GONE
        }

        if (!isRefreshing && newState == STATE_HOVERING) {
            onRefreshListener?.onRefresh()
            isRefreshing = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        this.fitsSystemWindows = ViewCompat.getFitsSystemWindows(parent as View)
        ViewCompat.requestApplyInsets(this)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (!checkLayoutParamsToSetRefresh(this)) {
            checkLayoutParamsToSetRefresh(parent as View)
        }
        refreshHeader?.setRefreshCallback(this)
        refreshHeader?.setRefreshEnable(isEnabled)
    }

    private fun checkLayoutParamsToSetRefresh(view: View): Boolean{
        val lp = view.layoutParams
        if (lp is CoordinatorLayout.LayoutParams) {
            val behavior = lp.behavior
            if (behavior is RefreshHeader) {
                this.refreshHeader = behavior
            }
            return true
        }
        return false
    }

    fun stopRefresh() {
        isRefreshing = false
        refreshHeader?.stopRefresh()
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

}
