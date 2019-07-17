package com.todou.nestrefresh

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.*
import android.support.annotation.IntRange
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.math.MathUtils
import android.support.v4.util.ObjectsCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import com.todou.nestrefresh.base.ViewOffsetHelper
import com.todou.nestrefresh.material.NRCollapsingTextHelper
import com.todou.nestrefresh.material.animation.NRAnimationUtils
import com.todou.nestrefresh.material.utils.NRDescendantOffsetUtils

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import android.view.ViewGroup.LayoutParams.MATCH_PARENT

class NRCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var refreshToolbar = true
    private val toolbarId: Int
    private var toolbar: Toolbar? = null
    private var toolbarDirectChild: View? = null
    private var dummyView: View? = null

    private var expandedMarginStart: Int = 0
    private var expandedMarginTop: Int = 0
    private var expandedMarginEnd: Int = 0
    private var expandedMarginBottom: Int = 0

    private val tmpRect = Rect()
    internal val collapsingTextHelper: NRCollapsingTextHelper?
    private var collapsingTitleEnabled: Boolean = false
    private var drawCollapsingTitle: Boolean = false

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see .setContentScrim
     */
    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable the
     * scrim functionality.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see .getContentScrim
     */
    var contentScrim: Drawable? = null
        set(drawable) {
            if (this.contentScrim !== drawable) {
                if (this.contentScrim != null) {
                    this.contentScrim!!.callback = null
                }
                field = drawable?.mutate()
                if (this.contentScrim != null) {
                    this.contentScrim!!.setBounds(0, 0, width, height)
                    this.contentScrim!!.callback = this
                    this.contentScrim!!.alpha = this.scrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    internal var statusBarScrim: Drawable? = null
    internal var scrimAlpha: Int = 0
        set(alpha) {
            if (alpha != this.scrimAlpha) {
                val contentScrim = this.contentScrim
                if (contentScrim != null && toolbar != null) {
                    ViewCompat.postInvalidateOnAnimation(toolbar!!)
                }
                field = alpha
                ViewCompat.postInvalidateOnAnimation(this@NRCollapsingToolbarLayout)
            }
        }
    private var scrimsAreShown: Boolean = false
    private var scrimAnimator: ValueAnimator? = null
    /**
     * Returns the duration in milliseconds used for scrim visibility animations.
     */
    /**
     * Set the duration used for scrim visibility animations.
     *
     * @param duration the duration to use in milliseconds
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_scrimAnimationDuration
     */
    var scrimAnimationDuration: Long = 0
    private var scrimVisibleHeightTrigger = -1

    private var onOffsetChangedListener: RefreshBarLayout.OffsetChangedListener? = null

    internal var currentOffset: Int = 0

    internal var lastInsets: WindowInsetsCompat? = null

    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return `null`.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_title
     */
    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_title
     * @see .setTitleEnabled
     * @see .getTitle
     */
    var title: CharSequence?
        get() = if (collapsingTitleEnabled) collapsingTextHelper!!.text else null
        set(title) {
            collapsingTextHelper!!.text = title
            updateContentDescriptionFromTitle()
        }

    /**
     * Returns whether this view is currently displaying its own title.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_titleEnabled
     * @see .setTitleEnabled
     */
    /**
     * Sets whether this view should display its own title.
     *
     *
     * The title displayed by this view will shrink and grow based on the scroll offset.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_titleEnabled
     * @see .isTitleEnabled
     */
    var isTitleEnabled: Boolean
        get() = collapsingTitleEnabled
        set(enabled) {
            if (enabled != collapsingTitleEnabled) {
                collapsingTitleEnabled = enabled
                updateContentDescriptionFromTitle()
                updateDummyView()
                requestLayout()
            }
        }

    /**
     * Returns the horizontal and vertical alignment for title when collapsed.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_collapsedTitleGravity
     */
    /**
     * Sets the horizontal alignment of the collapsed title and the vertical gravity that will be used
     * when there is extra space in the collapsed bounds beyond what is required for the title itself.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_collapsedTitleGravity
     */
    var collapsedTitleGravity: Int
        get() = collapsingTextHelper!!.collapsedTextGravity
        set(gravity) {
            collapsingTextHelper!!.collapsedTextGravity = gravity
        }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleGravity
     */
    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will be used
     * when there is extra space in the expanded bounds beyond what is required for the title itself.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleGravity
     */
    var expandedTitleGravity: Int
        get() = collapsingTextHelper!!.expandedTextGravity
        set(gravity) {
            collapsingTextHelper!!.expandedTextGravity = gravity
        }

    /**
     * Returns the typeface used for the collapsed title.
     */
    /**
     * Set the typeface to use for the collapsed title.
     *
     * @param typeface typeface to use, or `null` to use the default.
     */
    var collapsedTitleTypeface: Typeface?
        get() = collapsingTextHelper!!.getCollapsedTypeface()
        set(typeface) = collapsingTextHelper!!.setCollapsedTypeface(typeface!!)

    /**
     * Returns the typeface used for the expanded title.
     */
    /**
     * Set the typeface to use for the expanded title.
     *
     * @param typeface typeface to use, or `null` to use the default.
     */
    var expandedTitleTypeface: Typeface?
        get() = collapsingTextHelper!!.getExpandedTypeface()
        set(typeface) = collapsingTextHelper!!.setExpandedTypeface(typeface!!)

    /**
     * @return the starting expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginStart
     * @see .setExpandedTitleMarginStart
     */
    /**
     * Sets the starting expanded title margin in pixels.
     *
     * @param margin the starting title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginStart
     * @see .getExpandedTitleMarginStart
     */
    var expandedTitleMarginStart: Int
        get() = expandedMarginStart
        set(margin) {
            expandedMarginStart = margin
            requestLayout()
        }

    /**
     * @return the top expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginTop
     * @see .setExpandedTitleMarginTop
     */
    /**
     * Sets the top expanded title margin in pixels.
     *
     * @param margin the top title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginTop
     * @see .getExpandedTitleMarginTop
     */
    var expandedTitleMarginTop: Int
        get() = expandedMarginTop
        set(margin) {
            expandedMarginTop = margin
            requestLayout()
        }

    /**
     * @return the ending expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginEnd
     * @see .setExpandedTitleMarginEnd
     */
    /**
     * Sets the ending expanded title margin in pixels.
     *
     * @param margin the ending title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginEnd
     * @see .getExpandedTitleMarginEnd
     */
    var expandedTitleMarginEnd: Int
        get() = expandedMarginEnd
        set(margin) {
            expandedMarginEnd = margin
            requestLayout()
        }

    /**
     * @return the bottom expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginBottom
     * @see .setExpandedTitleMarginBottom
     */
    /**
     * Sets the bottom expanded title margin in pixels.
     *
     * @param margin the bottom title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginBottom
     * @see .getExpandedTitleMarginBottom
     */
    var expandedTitleMarginBottom: Int
        get() = expandedMarginBottom
        set(margin) {
            expandedMarginBottom = margin
            requestLayout()
        }

    init {

        collapsingTextHelper = NRCollapsingTextHelper(this)
        collapsingTextHelper.setTextSizeInterpolator(NRAnimationUtils.DECELERATE_INTERPOLATOR)

        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.NRCollapsingToolbarLayout,
            defStyleAttr,
            R.style.NRCollapsingToolbar
        )

        collapsingTextHelper.expandedTextGravity = a.getInt(
            R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleGravity,
            GravityCompat.START or Gravity.BOTTOM
        )
        collapsingTextHelper.collapsedTextGravity = a.getInt(
            R.styleable.NRCollapsingToolbarLayout_nr_collapsedTitleGravity,
            GravityCompat.START or Gravity.CENTER_VERTICAL
        )

        expandedMarginBottom = a.getDimensionPixelSize(
            R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMargin, 0
        )
        expandedMarginEnd = expandedMarginBottom
        expandedMarginTop = expandedMarginEnd
        expandedMarginStart = expandedMarginTop

        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginStart)) {
            expandedMarginStart =
                a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginStart, 0)
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginEnd)) {
            expandedMarginEnd =
                a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginEnd, 0)
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginTop)) {
            expandedMarginTop =
                a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginTop, 0)
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginBottom)) {
            expandedMarginBottom =
                a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginBottom, 0)
        }

        collapsingTitleEnabled = a.getBoolean(R.styleable.NRCollapsingToolbarLayout_nr_titleEnabled, true)
        title = a.getText(R.styleable.NRCollapsingToolbarLayout_nr_title)

        // First load the default text appearances
        collapsingTextHelper.setExpandedTextAppearance(
            R.style.TextAppearance_Design_CollapsingToolbar_Expanded
        )
        collapsingTextHelper.setCollapsedTextAppearance(
            R.style.TextAppearance_AppCompat_Widget_ActionBar_Title
        )

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleTextAppearance)) {
            collapsingTextHelper.setExpandedTextAppearance(
                a.getResourceId(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleTextAppearance, 0)
            )
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_collapsedTitleTextAppearance)) {
            collapsingTextHelper.setCollapsedTextAppearance(
                a.getResourceId(R.styleable.NRCollapsingToolbarLayout_nr_collapsedTitleTextAppearance, 0)
            )
        }

        scrimVisibleHeightTrigger =
            a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_scrimVisibleHeightTrigger, -1)

        scrimAnimationDuration = a.getInt(
            R.styleable.NRCollapsingToolbarLayout_nr_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION
        ).toLong()

        contentScrim = a.getDrawable(R.styleable.NRCollapsingToolbarLayout_nr_contentScrim)
        setStatusBarScrim(a.getDrawable(R.styleable.NRCollapsingToolbarLayout_nr_statusBarScrim))

        toolbarId = a.getResourceId(R.styleable.NRCollapsingToolbarLayout_nr_toolbarId, -1)

        a.recycle()

        setWillNotDraw(false)

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsetsCompat ->
            onWindowInsetChanged(
                windowInsetsCompat
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Add an OffsetChangedListener if possible
        val parent = parent
        if (parent is RefreshBarLayout) {
            // Copy over from the ABL whether we should fit system windows
            this.fitsSystemWindows = ViewCompat.getFitsSystemWindows(parent as View)

            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = OffsetUpdateListener()
            }
            parent.addOnOffsetChangedListener(onOffsetChangedListener!!)

            // We're attached, so lets request an inset dispatch
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun onDetachedFromWindow() {
        // Remove our OffsetChangedListener if possible and it exists
        val parent = parent
        if (onOffsetChangedListener != null && parent is RefreshBarLayout) {
            parent.removeOnOffsetChangedListener(onOffsetChangedListener!!)
        }

        super.onDetachedFromWindow()
    }

    internal fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null

        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets
            requestLayout()
        }

        // Consume the insets. This is done so that child views with fitSystemWindows=true do not
        // get the default padding functionality from View
        return insets.consumeSystemWindowInsets()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar()
        if (toolbar == null && this.contentScrim != null && this.scrimAlpha > 0) {
            this.contentScrim!!.mutate().alpha = this.scrimAlpha
            this.contentScrim!!.draw(canvas)
        }

        // Let the collapsing text helper draw its text
        if (collapsingTitleEnabled && drawCollapsingTitle) {
            collapsingTextHelper!!.draw(canvas)
        }

        // Now draw the status bar scrim
        if (statusBarScrim != null && this.scrimAlpha > 0) {
            val topInset = if (lastInsets != null) lastInsets!!.systemWindowInsetTop else 0
            if (topInset > 0) {
                statusBarScrim!!.setBounds(0, -currentOffset, width, topInset - currentOffset)
                statusBarScrim!!.mutate().alpha = this.scrimAlpha
                statusBarScrim!!.draw(canvas)
            }
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim just before the Toolbar is drawn
        var invalidated = false
        if (this.contentScrim != null && this.scrimAlpha > 0 && isToolbarChild(child)) {
            this.contentScrim!!.mutate().alpha = this.scrimAlpha
            this.contentScrim!!.draw(canvas)
            invalidated = true
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (this.contentScrim != null) {
            this.contentScrim!!.setBounds(0, 0, w, h)
        }
    }

    private fun ensureToolbar() {
        if (!refreshToolbar) {
            return
        }

        // First clear out the current Toolbar
        this.toolbar = null
        toolbarDirectChild = null

        if (toolbarId != -1) {
            // If we have an ID set, try and find it and it's direct parent to us
            this.toolbar = findViewById(toolbarId)
            if (this.toolbar != null) {
                toolbarDirectChild = findDirectChild(this.toolbar!!)
            }
        }

        if (this.toolbar == null) {
            // If we don't have an ID, or couldn't find a Toolbar with the correct ID, try and find
            // one from our direct children
            var toolbar: Toolbar? = null
            var i = 0
            val count = childCount
            while (i < count) {
                val child = getChildAt(i)
                if (child is Toolbar) {
                    toolbar = child
                    break
                }
                i++
            }
            this.toolbar = toolbar
        }

        updateDummyView()
        refreshToolbar = false
    }

    private fun isToolbarChild(child: View): Boolean {
        return if (toolbarDirectChild == null || toolbarDirectChild === this)
            child === toolbar
        else
            child === toolbarDirectChild
    }

    /**
     * Returns the direct child of this layout, which itself is the ancestor of the given view.
     */
    private fun findDirectChild(descendant: View): View {
        var directChild = descendant
        var p: ViewParent? = descendant.parent
        while (p !== this && p != null) {
            if (p is View) {
                directChild = p
            }
            p = p.parent
        }
        return directChild
    }

    private fun updateDummyView() {
        if (!collapsingTitleEnabled && dummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            val parent = dummyView!!.parent
            if (parent is ViewGroup) {
                parent.removeView(dummyView)
            }
        }
        if (collapsingTitleEnabled && toolbar != null) {
            if (dummyView == null) {
                dummyView = View(context)
            }
            if (dummyView!!.parent == null) {
                toolbar!!.addView(dummyView, MATCH_PARENT, MATCH_PARENT)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        ensureToolbar()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val topInset = if (lastInsets != null) lastInsets!!.systemWindowInsetTop else 0
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            // If we have a top inset and we're set to wrap_content height we need to make sure
            // we add the top inset to our height, therefore we re-measure
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight + topInset, View.MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (lastInsets != null) {
            // Shift down any views which are not set to fit system windows
            val insetTop = lastInsets!!.systemWindowInsetTop
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.top < insetTop) {
                        // If the child isn't set to fit system windows but is drawing within
                        // the inset offset it down
                        ViewCompat.offsetTopAndBottom(child, insetTop)
                    }
                }
                i++
            }
        }

        // Update our child view offset helpers so that they track the correct layout coordinates
        run {
            var i = 0
            val z = childCount
            while (i < z) {
                getViewOffsetHelper(getChildAt(i)).onViewLayout()
                i++
            }
        }

        // Update the collapsed bounds by getting its transformed bounds
        if (collapsingTitleEnabled && dummyView != null) {
            // We only draw the title if the dummy view is being displayed (Toolbar removes
            // views if there is no space)
            drawCollapsingTitle = ViewCompat.isAttachedToWindow(dummyView!!) && dummyView!!.visibility == View.VISIBLE

            if (drawCollapsingTitle) {
                val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

                // Update the collapsed bounds
                val maxOffset = getMaxOffsetForPinChild(if (toolbarDirectChild != null) toolbarDirectChild else toolbar)
                NRDescendantOffsetUtils.getDescendantRect(this, dummyView!!, tmpRect)
                collapsingTextHelper!!.setCollapsedBounds(
                    tmpRect.left + if (isRtl) toolbar!!.titleMarginEnd else toolbar!!.titleMarginStart,
                    tmpRect.top + maxOffset + toolbar!!.titleMarginTop,
                    tmpRect.right + if (isRtl) toolbar!!.titleMarginStart else toolbar!!.titleMarginEnd,
                    tmpRect.bottom + maxOffset - toolbar!!.titleMarginBottom
                )

                // Update the expanded bounds
                collapsingTextHelper.setExpandedBounds(
                    if (isRtl) expandedMarginEnd else expandedMarginStart,
                    tmpRect.top + expandedMarginTop,
                    right - left - if (isRtl) expandedMarginStart else expandedMarginEnd,
                    bottom - top - expandedMarginBottom
                )
                // Now recalculate using the new bounds
                collapsingTextHelper.recalculate()
            }
        }

        // Set our minimum height to enable proper AppBarLayout collapsing
        if (toolbar != null) {
            if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper!!.text)) {
                // If we do not currently have a title, try and grab it from the Toolbar
                title = toolbar!!.title
            }
            if (toolbarDirectChild == null || toolbarDirectChild === this) {
                minimumHeight = getHeightWithMargins(toolbar!!)
            } else {
                minimumHeight = getHeightWithMargins(toolbarDirectChild!!)
            }
        }

        updateScrimVisibility()

        // Apply any view offsets, this should be done at the very end of layout
        var i = 0
        val z = childCount
        while (i < z) {
            getViewOffsetHelper(getChildAt(i)).updateOffsets()
            i++
        }
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
     * vertical scroll may overwrite this value.
     *
     * @param shown   whether the scrims should be shown
     * @param animate whether to animate the visibility change
     * @see .getStatusBarScrim
     * @see .getContentScrim
     */
    @JvmOverloads
    fun setScrimsShown(shown: Boolean, animate: Boolean = ViewCompat.isLaidOut(this) && !isInEditMode) {
        if (scrimsAreShown != shown) {
            if (animate) {
                animateScrim(if (shown) 0xFF else 0x0)
            } else {
                scrimAlpha = if (shown) 0xFF else 0x0
            }
            scrimsAreShown = shown
        }
    }

    private fun animateScrim(targetAlpha: Int) {
        ensureToolbar()
        if (scrimAnimator == null) {
            scrimAnimator = ValueAnimator()
            scrimAnimator!!.duration = scrimAnimationDuration
            scrimAnimator!!.interpolator = if (targetAlpha > this.scrimAlpha)
                NRAnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
            else
                NRAnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
            scrimAnimator!!.addUpdateListener { animator -> scrimAlpha = animator.animatedValue as Int }
        } else if (scrimAnimator!!.isRunning) {
            scrimAnimator!!.cancel()
        }

        scrimAnimator!!.setIntValues(this.scrimAlpha, targetAlpha)
        scrimAnimator!!.start()
    }

    /**
     * Set the color to use for the content scrim.
     *
     * @param color the color to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see .getContentScrim
     */
    fun setContentScrimColor(@ColorInt color: Int) {
        contentScrim = ColorDrawable(color)
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see .getContentScrim
     */
    fun setContentScrimResource(@DrawableRes resId: Int) {
        contentScrim = ContextCompat.getDrawable(context, resId)
    }

    /**
     * Set the drawable to use for the status bar scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     *
     * This scrim is only shown when we have been given a top system inset.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see .getStatusBarScrim
     */
    fun setStatusBarScrim(drawable: Drawable?) {
        if (statusBarScrim !== drawable) {
            if (statusBarScrim != null) {
                statusBarScrim!!.callback = null
            }
            statusBarScrim = drawable?.mutate()
            if (statusBarScrim != null) {
                if (statusBarScrim!!.isStateful) {
                    statusBarScrim!!.state = drawableState
                }
                DrawableCompat.setLayoutDirection(statusBarScrim!!, ViewCompat.getLayoutDirection(this))
                statusBarScrim!!.setVisible(visibility == View.VISIBLE, false)
                statusBarScrim!!.callback = this
                statusBarScrim!!.alpha = this.scrimAlpha
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        val state = drawableState
        var changed = false

        var d = statusBarScrim
        if (d != null && d.isStateful) {
            changed = changed or d.setState(state)
        }
        d = this.contentScrim
        if (d != null && d.isStateful) {
            changed = changed or d.setState(state)
        }
        if (collapsingTextHelper != null) {
            changed = changed or collapsingTextHelper.setState(state)
        }

        if (changed) {
            invalidate()
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === this.contentScrim || who === statusBarScrim
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        val visible = visibility == View.VISIBLE
        if (statusBarScrim != null && statusBarScrim!!.isVisible != visible) {
            statusBarScrim!!.setVisible(visible, false)
        }
        if (this.contentScrim != null && this.contentScrim!!.isVisible != visible) {
            this.contentScrim!!.setVisible(visible, false)
        }
    }

    /**
     * Set the color to use for the status bar scrim.
     *
     *
     * This scrim is only shown when we have been given a top system inset.
     *
     * @param color the color to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see .getStatusBarScrim
     */
    fun setStatusBarScrimColor(@ColorInt color: Int) {
        setStatusBarScrim(ColorDrawable(color))
    }

    /**
     * Set the drawable to use for the status bar scrim from resources.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see .getStatusBarScrim
     */
    fun setStatusBarScrimResource(@DrawableRes resId: Int) {
        setStatusBarScrim(ContextCompat.getDrawable(context, resId))
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see .setStatusBarScrim
     */
    fun getStatusBarScrim(): Drawable? {
        return statusBarScrim
    }

    /**
     * Sets the text color and size for the collapsed title from the specified NRTextAppearance
     * resource.
     *
     * @attr ref
     * com.google.android.material.R.styleable#NRCollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    fun setCollapsedTitleTextAppearance(@StyleRes resId: Int) {
        collapsingTextHelper!!.setCollapsedTextAppearance(resId)
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    fun setCollapsedTitleTextColor(@ColorInt color: Int) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color))
    }

    /**
     * Sets the text colors of the collapsed title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    fun setCollapsedTitleTextColor(colors: ColorStateList) {
        collapsingTextHelper!!.setCollapsedTextColor(colors)
    }

    /**
     * Sets the text color and size for the expanded title from the specified NRTextAppearance resource.
     *
     * @attr ref
     * com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleTextAppearance
     */
    fun setExpandedTitleTextAppearance(@StyleRes resId: Int) {
        collapsingTextHelper!!.setExpandedTextAppearance(resId)
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    fun setExpandedTitleColor(@ColorInt color: Int) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color))
    }

    /**
     * Sets the text colors of the expanded title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    fun setExpandedTitleTextColor(colors: ColorStateList) {
        collapsingTextHelper!!.setExpandedTextColor(colors)
    }

    /**
     * Sets the expanded title margins.
     *
     * @param start  the starting title margin in pixels
     * @param top    the top title margin in pixels
     * @param end    the ending title margin in pixels
     * @param bottom the bottom title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMargin
     * @see .getExpandedTitleMarginStart
     * @see .getExpandedTitleMarginTop
     * @see .getExpandedTitleMarginEnd
     * @see .getExpandedTitleMarginBottom
     */
    fun setExpandedTitleMargin(start: Int, top: Int, end: Int, bottom: Int) {
        expandedMarginStart = start
        expandedMarginTop = top
        expandedMarginEnd = end
        expandedMarginBottom = bottom
        requestLayout()
    }

    /**
     * Set the amount of visible height in pixels used to define when to trigger a scrim visibility
     * change.
     *
     *
     * If the visible height of this view is less than the given value, the scrims will be made
     * visible, otherwise they are hidden.
     *
     * @param height value in pixels used to define when to trigger a scrim visibility change
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_scrimVisibleHeightTrigger
     */
    fun setScrimVisibleHeightTrigger(@IntRange(from = 0) height: Int) {
        if (scrimVisibleHeightTrigger != height) {
            scrimVisibleHeightTrigger = height
            // Update the scrim visibility
            updateScrimVisibility()
        }
    }

    /**
     * Returns the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     * @see .setScrimVisibleHeightTrigger
     */
    fun getScrimVisibleHeightTrigger(): Int {
        if (scrimVisibleHeightTrigger >= 0) {
            // If we have one explicitly set, return it
            return scrimVisibleHeightTrigger
        }

        // Otherwise we'll use the default computed value
        val insetTop = if (lastInsets != null) lastInsets!!.systemWindowInsetTop else 0

        val minHeight = ViewCompat.getMinimumHeight(this)
        return if (minHeight > 0) {
            // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
            Math.min(minHeight * 2 + insetTop, height)
        } else height / 3

        // If we reach here then we don't have a min height set. Instead we'll take a
        // guess at 1/3 of our height being visible
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): FrameLayout.LayoutParams {
        return LayoutParams(p)
    }

    class LayoutParams : FrameLayout.LayoutParams {

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of [.COLLAPSE_MODE_OFF], [.COLLAPSE_MODE_PIN] or
         * [.COLLAPSE_MODE_PARALLAX].
         */
        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of [.COLLAPSE_MODE_OFF], [.COLLAPSE_MODE_PIN] or [                     ][.COLLAPSE_MODE_PARALLAX].
         */
        @get:CollapseMode
        var collapseMode = COLLAPSE_MODE_OFF
        /**
         * Returns the parallax scroll multiplier used in conjunction with [ ][.COLLAPSE_MODE_PARALLAX].
         *
         * @see .setParallaxMultiplier
         */
        /**
         * Set the parallax scroll multiplier used in conjunction with [.COLLAPSE_MODE_PARALLAX].
         * A value of `0.0` indicates no movement at all, `1.0f` indicates normal scroll
         * movement.
         *
         * @param multiplier the multiplier.
         * @see .getParallaxMultiplier
         */
        var parallaxMultiplier = DEFAULT_PARALLAX_MULTIPLIER

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {

            val a = c.obtainStyledAttributes(attrs, R.styleable.NRCollapsingToolbarLayout_Layout)
            collapseMode = a.getInt(
            R.styleable.NRCollapsingToolbarLayout_Layout_nr_layout_collapseMode, COLLAPSE_MODE_OFF)
            parallaxMultiplier = a.getFloat(
            R.styleable.NRCollapsingToolbarLayout_Layout_nr_layout_collapseParallaxMultiplier,
            DEFAULT_PARALLAX_MULTIPLIER)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height) {}

        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity) {}

        constructor(p: ViewGroup.LayoutParams) : super(p) {}

        constructor(source: ViewGroup.MarginLayoutParams) : super(source) {}

        @RequiresApi(19)
        constructor(source: FrameLayout.LayoutParams) : super(source) {
        }// The copy constructor called here only exists on API 19+.

        companion object {

            private val DEFAULT_PARALLAX_MULTIPLIER = 0.5f

            /**
             * The view will act as normal with no collapsing behavior.
             */
            const val COLLAPSE_MODE_OFF = 0

            /**
             * The view will pin in place until it reaches the bottom of the [ ].
             */
            const val COLLAPSE_MODE_PIN = 1

            /**
             * The view will scroll in a parallax fashion. See [.setParallaxMultiplier] to
             * change the multiplier used.
             */
            const val COLLAPSE_MODE_PARALLAX = 2

            @IntDef(COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX)
            @Retention(RetentionPolicy.SOURCE)
            annotation class CollapseMode
        }
    }

    /**
     * Show or hide the scrims if needed
     */
    internal fun updateScrimVisibility() {
        if (this.contentScrim != null || statusBarScrim != null) {
            setScrimsShown(height + currentOffset < getScrimVisibleHeightTrigger())
        }
    }

    internal fun getMaxOffsetForPinChild(child: View?): Int {
        val offsetHelper = getViewOffsetHelper(child!!)
        val lp = child.layoutParams as LayoutParams
        return height - offsetHelper.layoutTop - child.height - lp.bottomMargin
    }

    private fun updateContentDescriptionFromTitle() {
        // Set this layout's contentDescription to match the title if it's shown by CollapsingTextHelper
        contentDescription = title
    }

    private inner class OffsetUpdateListener internal constructor() : RefreshBarLayout.OffsetChangedListener {

        override fun onOffsetChanged(layout: RefreshBarLayout, verticalOffset: Int) {
            currentOffset = verticalOffset

            val insetTop = if (lastInsets != null) lastInsets!!.systemWindowInsetTop else 0

            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                val offsetHelper = getViewOffsetHelper(child)

                when (lp.collapseMode) {
                    LayoutParams.COLLAPSE_MODE_PIN -> offsetHelper.setTopAndBottomOffset(
                        MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child))
                    )
                    LayoutParams.COLLAPSE_MODE_PARALLAX -> if (verticalOffset > 0) {
                        val parent = layout.parent as View
                        val scale = 1f + 1f * verticalOffset / parent.height
                        child.scaleX = scale
                        child.scaleY = scale
                    } else {
                        offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMultiplier))
                    }
                    else -> {
                    }
                }
                i++
            }

            // Show or hide the scrims if needed
            updateScrimVisibility()

            if (statusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(this@NRCollapsingToolbarLayout)
            }
            val expandRange = height - ViewCompat.getMinimumHeight(this@NRCollapsingToolbarLayout) - insetTop
            collapsingTextHelper!!.expansionFraction =
                Math.abs(if (verticalOffset > 0) 0 else verticalOffset) / expandRange.toFloat()
        }
    }

    companion object {

        private val DEFAULT_SCRIM_ANIMATION_DURATION = 600

        private fun getHeightWithMargins(view: View): Int {
            val lp = view.layoutParams
            return if (lp is ViewGroup.MarginLayoutParams) {
                view.height + lp.topMargin + lp.bottomMargin
            } else view.height
        }

        internal fun getViewOffsetHelper(view: View): ViewOffsetHelper {
            var offsetHelper: ViewOffsetHelper? = view.getTag(R.id.view_offset_helper) as ViewOffsetHelper
            if (offsetHelper == null) {
                offsetHelper = ViewOffsetHelper(view)
                view.setTag(R.id.view_offset_helper, offsetHelper)
            }
            return offsetHelper
        }
    }
}
/**
 * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
 * vertical scroll may overwrite this value. Any visibility change will be animated if this view
 * has already been laid out.
 *
 * @param shown whether the scrims should be shown
 * @see .getStatusBarScrim
 * @see .getContentScrim
 */
