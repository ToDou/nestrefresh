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
import com.todou.nestrefresh.base.RefreshHeaderBehavior
import android.support.design.widget.CoordinatorLayout
import android.view.ViewGroup
import java.lang.reflect.Field


class RefreshHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), RefreshCallback {

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
    private var onRefreshListener: OnRefreshListener? = null
    private var behavior: RefreshBehavior? = null
    private var showStatusInset: Boolean

    init {
        init(context, attrs, defStyleAttr)

        val a = context.obtainStyledAttributes(attrs, R.styleable.RefreshHeaderView)
        showStatusInset = a.getBoolean(
            R.styleable.RefreshHeaderView_status_bar_inset_visible, false
        )
        a.recycle()
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

        if (newState == RefreshHeaderBehavior.STATE_HOVERING) {
            onRefreshListener?.onRefresh()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (layoutParams is MarginLayoutParams) {
            val inset = getInsetHeight()
            inset.takeIf { it > 0 }
                ?.let {
                    val lp = layoutParams as MarginLayoutParams
                    lp.topMargin = it
                    layoutParams = lp
                }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        val lp = layoutParams
        if (lp is CoordinatorLayout.LayoutParams) {
            val behavior = lp.behavior
            if (behavior is RefreshBehavior) {
                this.behavior = behavior
                behavior.setRefreshCallback(this)
                behavior.setRefreshEnable(isEnabled)
            }
        }
    }

    fun setRefresh(refreshing: Boolean) {
        behavior?.setState(
            if (refreshing)
                RefreshHeaderBehavior.STATE_HOVERING
            else
                RefreshHeaderBehavior.STATE_COLLAPSED
        )
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    private fun getInsetHeight(): Int {
        return if (showStatusInset) getStatusBarHeight(context) else 0
    }

    private fun getStatusBarHeight(context: Context): Int {
        val c: Class<*>
        val obj: Any
        val field: Field

        val x: Int
        var statusBarHeight = 0
        try {
            c = Class.forName("com.android.internal.R\$dimen")
            obj = c.newInstance()
            field = c.getField("status_bar_height")
            x = Integer.parseInt(field.get(obj).toString())
            statusBarHeight = context.resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }

        return statusBarHeight
    }

}
