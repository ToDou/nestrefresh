package com.todou.nestrefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.*;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.math.MathUtils;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.todou.nestrefresh.base.ViewOffsetHelper;
import com.todou.nestrefresh.material.NRCollapsingTextHelper;
import com.todou.nestrefresh.material.animation.NRAnimationUtils;
import com.todou.nestrefresh.material.utils.NRDescendantOffsetUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

public class NRCollapsingToolbarLayout extends FrameLayout {

    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;

    private boolean refreshToolbar = true;
    private int toolbarId;
    private Toolbar toolbar;
    private View toolbarDirectChild;
    private View dummyView;

    private int expandedMarginStart;
    private int expandedMarginTop;
    private int expandedMarginEnd;
    private int expandedMarginBottom;

    private final Rect tmpRect = new Rect();
    final NRCollapsingTextHelper collapsingTextHelper;
    private boolean collapsingTitleEnabled;
    private boolean drawCollapsingTitle;

    private Drawable contentScrim;
    Drawable statusBarScrim;
    private int scrimAlpha;
    private boolean scrimsAreShown;
    private ValueAnimator scrimAnimator;
    private long scrimAnimationDuration;
    private int scrimVisibleHeightTrigger = -1;

    private RefreshStickyLayout.OffsetChangedListener onOffsetChangedListener;

    int currentOffset;

    WindowInsetsCompat lastInsets;

    public NRCollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public NRCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NRCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        collapsingTextHelper = new NRCollapsingTextHelper(this);
        collapsingTextHelper.setTextSizeInterpolator(NRAnimationUtils.DECELERATE_INTERPOLATOR);

        TypedArray a =
                context.obtainStyledAttributes(
                        attrs,
                        R.styleable.NRCollapsingToolbarLayout,
                        defStyleAttr,
                        R.style.NRCollapsingToolbar);

        collapsingTextHelper.setExpandedTextGravity(
                a.getInt(
                        R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleGravity,
                        GravityCompat.START | Gravity.BOTTOM));
        collapsingTextHelper.setCollapsedTextGravity(
                a.getInt(
                        R.styleable.NRCollapsingToolbarLayout_nr_collapsedTitleGravity,
                        GravityCompat.START | Gravity.CENTER_VERTICAL));

        expandedMarginStart =
                expandedMarginTop =
                        expandedMarginEnd =
                                expandedMarginBottom =
                                        a.getDimensionPixelSize(
                                                R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMargin, 0);

        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginStart)) {
            expandedMarginStart =
                    a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginStart, 0);
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginEnd)) {
            expandedMarginEnd =
                    a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginEnd, 0);
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginTop)) {
            expandedMarginTop =
                    a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginBottom)) {
            expandedMarginBottom =
                    a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleMarginBottom, 0);
        }

        collapsingTitleEnabled = a.getBoolean(R.styleable.NRCollapsingToolbarLayout_nr_titleEnabled, true);
        setTitle(a.getText(R.styleable.NRCollapsingToolbarLayout_nr_title));

        // First load the default text appearances
        collapsingTextHelper.setExpandedTextAppearance(
                R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
        collapsingTextHelper.setCollapsedTextAppearance(
                R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleTextAppearance)) {
            collapsingTextHelper.setExpandedTextAppearance(
                    a.getResourceId(R.styleable.NRCollapsingToolbarLayout_nr_expandedTitleTextAppearance, 0));
        }
        if (a.hasValue(R.styleable.NRCollapsingToolbarLayout_nr_collapsedTitleTextAppearance)) {
            collapsingTextHelper.setCollapsedTextAppearance(
                    a.getResourceId(R.styleable.NRCollapsingToolbarLayout_nr_collapsedTitleTextAppearance, 0));
        }

        scrimVisibleHeightTrigger =
                a.getDimensionPixelSize(R.styleable.NRCollapsingToolbarLayout_nr_scrimVisibleHeightTrigger, -1);

        scrimAnimationDuration =
                a.getInt(
                        R.styleable.NRCollapsingToolbarLayout_nr_scrimAnimationDuration,
                        DEFAULT_SCRIM_ANIMATION_DURATION);

        setContentScrim(a.getDrawable(R.styleable.NRCollapsingToolbarLayout_nr_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.NRCollapsingToolbarLayout_nr_statusBarScrim));

        toolbarId = a.getResourceId(R.styleable.NRCollapsingToolbarLayout_nr_toolbarId, -1);

        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this, new android.support.v4.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                return onWindowInsetChanged(windowInsetsCompat);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof RefreshStickyLayout) {
            // Copy over from the ABL whether we should fit system windows
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));

            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = new OffsetUpdateListener();
            }
            ((RefreshStickyLayout) parent).addOnOffsetChangedListener(onOffsetChangedListener);

            // We're attached, so lets request an inset dispatch
            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (onOffsetChangedListener != null && parent instanceof RefreshStickyLayout) {
            ((RefreshStickyLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets;
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets;
            requestLayout();
        }

        // Consume the insets. This is done so that child views with fitSystemWindows=true do not
        // get the default padding functionality from View
        return insets.consumeSystemWindowInsets();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar();
        if (toolbar == null && contentScrim != null && scrimAlpha > 0) {
            contentScrim.mutate().setAlpha(scrimAlpha);
            contentScrim.draw(canvas);
        }

        // Let the collapsing text helper draw its text
        if (collapsingTitleEnabled && drawCollapsingTitle) {
            collapsingTextHelper.draw(canvas);
        }

        // Now draw the status bar scrim
        if (statusBarScrim != null && scrimAlpha > 0) {
            final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                statusBarScrim.setBounds(0, -currentOffset, getWidth(), topInset - currentOffset);
                statusBarScrim.mutate().setAlpha(scrimAlpha);
                statusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim just before the Toolbar is drawn
        boolean invalidated = false;
        if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
            contentScrim.mutate().setAlpha(scrimAlpha);
            contentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (contentScrim != null) {
            contentScrim.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (!refreshToolbar) {
            return;
        }

        // First clear out the current Toolbar
        this.toolbar = null;
        toolbarDirectChild = null;

        if (toolbarId != -1) {
            // If we have an ID set, try and find it and it's direct parent to us
            this.toolbar = findViewById(toolbarId);
            if (this.toolbar != null) {
                toolbarDirectChild = findDirectChild(this.toolbar);
            }
        }

        if (this.toolbar == null) {
            // If we don't have an ID, or couldn't find a Toolbar with the correct ID, try and find
            // one from our direct children
            Toolbar toolbar = null;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof Toolbar) {
                    toolbar = (Toolbar) child;
                    break;
                }
            }
            this.toolbar = toolbar;
        }

        updateDummyView();
        refreshToolbar = false;
    }

    private boolean isToolbarChild(View child) {
        return (toolbarDirectChild == null || toolbarDirectChild == this)
                ? child == toolbar
                : child == toolbarDirectChild;
    }

    /**
     * Returns the direct child of this layout, which itself is the ancestor of the given view.
     */
    private View findDirectChild(final View descendant) {
        View directChild = descendant;
        for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
            if (p instanceof View) {
                directChild = (View) p;
            }
        }
        return directChild;
    }

    private void updateDummyView() {
        if (!collapsingTitleEnabled && dummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            final ViewParent parent = dummyView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(dummyView);
            }
        }
        if (collapsingTitleEnabled && toolbar != null) {
            if (dummyView == null) {
                dummyView = new View(getContext());
            }
            if (dummyView.getParent() == null) {
                toolbar.addView(dummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int mode = MeasureSpec.getMode(heightMeasureSpec);
        final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            // If we have a top inset and we're set to wrap_content height we need to make sure
            // we add the top inset to our height, therefore we re-measure
            heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (lastInsets != null) {
            // Shift down any views which are not set to fit system windows
            final int insetTop = lastInsets.getSystemWindowInsetTop();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.getTop() < insetTop) {
                        // If the child isn't set to fit system windows but is drawing within
                        // the inset offset it down
                        ViewCompat.offsetTopAndBottom(child, insetTop);
                    }
                }
            }
        }

        // Update our child view offset helpers so that they track the correct layout coordinates
        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        }

        // Update the collapsed bounds by getting its transformed bounds
        if (collapsingTitleEnabled && dummyView != null) {
            // We only draw the title if the dummy view is being displayed (Toolbar removes
            // views if there is no space)
            drawCollapsingTitle =
                    ViewCompat.isAttachedToWindow(dummyView) && dummyView.getVisibility() == VISIBLE;

            if (drawCollapsingTitle) {
                final boolean isRtl =
                        ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

                // Update the collapsed bounds
                final int maxOffset =
                        getMaxOffsetForPinChild(toolbarDirectChild != null ? toolbarDirectChild : toolbar);
                NRDescendantOffsetUtils.getDescendantRect(this, dummyView, tmpRect);
                collapsingTextHelper.setCollapsedBounds(
                        tmpRect.left + (isRtl ? toolbar.getTitleMarginEnd() : toolbar.getTitleMarginStart()),
                        tmpRect.top + maxOffset + toolbar.getTitleMarginTop(),
                        tmpRect.right + (isRtl ? toolbar.getTitleMarginStart() : toolbar.getTitleMarginEnd()),
                        tmpRect.bottom + maxOffset - toolbar.getTitleMarginBottom());

                // Update the expanded bounds
                collapsingTextHelper.setExpandedBounds(
                        isRtl ? expandedMarginEnd : expandedMarginStart,
                        tmpRect.top + expandedMarginTop,
                        right - left - (isRtl ? expandedMarginStart : expandedMarginEnd),
                        bottom - top - expandedMarginBottom);
                // Now recalculate using the new bounds
                collapsingTextHelper.recalculate();
            }
        }

        // Set our minimum height to enable proper AppBarLayout collapsing
        if (toolbar != null) {
            if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper.getText())) {
                // If we do not currently have a title, try and grab it from the Toolbar
                setTitle(toolbar.getTitle());
            }
            if (toolbarDirectChild == null || toolbarDirectChild == this) {
                setMinimumHeight(getHeightWithMargins(toolbar));
            } else {
                setMinimumHeight(getHeightWithMargins(toolbarDirectChild));
            }
        }

        updateScrimVisibility();

        // Apply any view offsets, this should be done at the very end of layout
        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).updateOffsets();
        }
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
        }
        return view.getHeight();
    }

    static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_title
     * @see #setTitleEnabled(boolean)
     * @see #getTitle()
     */
    public void setTitle(@Nullable CharSequence title) {
        collapsingTextHelper.setText(title);
        updateContentDescriptionFromTitle();
    }

    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return {@code null}.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_title
     */
    @Nullable
    public CharSequence getTitle() {
        return collapsingTitleEnabled ? collapsingTextHelper.getText() : null;
    }

    /**
     * Sets whether this view should display its own title.
     *
     * <p>The title displayed by this view will shrink and grow based on the scroll offset.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_titleEnabled
     * @see #isTitleEnabled()
     */
    public void setTitleEnabled(boolean enabled) {
        if (enabled != collapsingTitleEnabled) {
            collapsingTitleEnabled = enabled;
            updateContentDescriptionFromTitle();
            updateDummyView();
            requestLayout();
        }
    }

    /**
     * Returns whether this view is currently displaying its own title.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_titleEnabled
     * @see #setTitleEnabled(boolean)
     */
    public boolean isTitleEnabled() {
        return collapsingTitleEnabled;
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
     * vertical scroll may overwrite this value. Any visibility change will be animated if this view
     * has already been laid out.
     *
     * @param shown whether the scrims should be shown
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
     * vertical scroll may overwrite this value.
     *
     * @param shown   whether the scrims should be shown
     * @param animate whether to animate the visibility change
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown, boolean animate) {
        if (scrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            scrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (scrimAnimator == null) {
            scrimAnimator = new ValueAnimator();
            scrimAnimator.setDuration(scrimAnimationDuration);
            scrimAnimator.setInterpolator(
                    targetAlpha > scrimAlpha
                            ? NRAnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
                            : NRAnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            scrimAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            setScrimAlpha((int) animator.getAnimatedValue());
                        }
                    });
        } else if (scrimAnimator.isRunning()) {
            scrimAnimator.cancel();
        }

        scrimAnimator.setIntValues(scrimAlpha, targetAlpha);
        scrimAnimator.start();
    }

    void setScrimAlpha(int alpha) {
        if (alpha != scrimAlpha) {
            final Drawable contentScrim = this.contentScrim;
            if (contentScrim != null && toolbar != null) {
                ViewCompat.postInvalidateOnAnimation(toolbar);
            }
            scrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(NRCollapsingToolbarLayout.this);
        }
    }

    int getScrimAlpha() {
        return scrimAlpha;
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable the
     * scrim functionality.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrim(@Nullable Drawable drawable) {
        if (contentScrim != drawable) {
            if (contentScrim != null) {
                contentScrim.setCallback(null);
            }
            contentScrim = drawable != null ? drawable.mutate() : null;
            if (contentScrim != null) {
                contentScrim.setBounds(0, 0, getWidth(), getHeight());
                contentScrim.setCallback(this);
                contentScrim.setAlpha(scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the content scrim.
     *
     * @param color the color to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_contentScrim
     * @see #setContentScrim(Drawable)
     */
    @Nullable
    public Drawable getContentScrim() {
        return contentScrim;
    }

    /**
     * Set the drawable to use for the status bar scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (statusBarScrim != drawable) {
            if (statusBarScrim != null) {
                statusBarScrim.setCallback(null);
            }
            statusBarScrim = drawable != null ? drawable.mutate() : null;
            if (statusBarScrim != null) {
                if (statusBarScrim.isStateful()) {
                    statusBarScrim.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(statusBarScrim, ViewCompat.getLayoutDirection(this));
                statusBarScrim.setVisible(getVisibility() == VISIBLE, false);
                statusBarScrim.setCallback(this);
                statusBarScrim.setAlpha(scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        Drawable d = statusBarScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        d = contentScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        if (collapsingTextHelper != null) {
            changed |= collapsingTextHelper.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == contentScrim || who == statusBarScrim;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (statusBarScrim != null && statusBarScrim.isVisible() != visible) {
            statusBarScrim.setVisible(visible, false);
        }
        if (contentScrim != null && contentScrim.isVisible() != visible) {
            contentScrim.setVisible(visible, false);
        }
    }

    /**
     * Set the color to use for the status bar scrim.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param color the color to display
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the status bar scrim from resources.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * @attr ref R.styleable#NRCollapsingToolbarLayout_statusBarScrim
     * @see #setStatusBarScrim(Drawable)
     */
    @Nullable
    public Drawable getStatusBarScrim() {
        return statusBarScrim;
    }

    /**
     * Sets the text color and size for the collapsed title from the specified NRTextAppearance
     * resource.
     *
     * @attr ref
     * com.google.android.material.R.styleable#NRCollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        collapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    public void setCollapsedTitleTextColor(@ColorInt int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text colors of the collapsed title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
        collapsingTextHelper.setCollapsedTextColor(colors);
    }

    /**
     * Sets the horizontal alignment of the collapsed title and the vertical gravity that will be used
     * when there is extra space in the collapsed bounds beyond what is required for the title itself.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_collapsedTitleGravity
     */
    public void setCollapsedTitleGravity(int gravity) {
        collapsingTextHelper.setCollapsedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when collapsed.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_collapsedTitleGravity
     */
    public int getCollapsedTitleGravity() {
        return collapsingTextHelper.getCollapsedTextGravity();
    }

    /**
     * Sets the text color and size for the expanded title from the specified NRTextAppearance resource.
     *
     * @attr ref
     * com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleTextAppearance
     */
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        collapsingTextHelper.setExpandedTextAppearance(resId);
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    public void setExpandedTitleColor(@ColorInt int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text colors of the expanded title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
        collapsingTextHelper.setExpandedTextColor(colors);
    }

    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will be used
     * when there is extra space in the expanded bounds beyond what is required for the title itself.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleGravity
     */
    public void setExpandedTitleGravity(int gravity) {
        collapsingTextHelper.setExpandedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     *
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleGravity
     */
    public int getExpandedTitleGravity() {
        return collapsingTextHelper.getExpandedTextGravity();
    }

    /**
     * Set the typeface to use for the collapsed title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        collapsingTextHelper.setCollapsedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the collapsed title.
     */
    @NonNull
    public Typeface getCollapsedTitleTypeface() {
        return collapsingTextHelper.getCollapsedTypeface();
    }

    /**
     * Set the typeface to use for the expanded title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        collapsingTextHelper.setExpandedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the expanded title.
     */
    @NonNull
    public Typeface getExpandedTitleTypeface() {
        return collapsingTextHelper.getExpandedTypeface();
    }

    /**
     * Sets the expanded title margins.
     *
     * @param start  the starting title margin in pixels
     * @param top    the top title margin in pixels
     * @param end    the ending title margin in pixels
     * @param bottom the bottom title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMargin
     * @see #getExpandedTitleMarginStart()
     * @see #getExpandedTitleMarginTop()
     * @see #getExpandedTitleMarginEnd()
     * @see #getExpandedTitleMarginBottom()
     */
    public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
        expandedMarginStart = start;
        expandedMarginTop = top;
        expandedMarginEnd = end;
        expandedMarginBottom = bottom;
        requestLayout();
    }

    /**
     * @return the starting expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginStart
     * @see #setExpandedTitleMarginStart(int)
     */
    public int getExpandedTitleMarginStart() {
        return expandedMarginStart;
    }

    /**
     * Sets the starting expanded title margin in pixels.
     *
     * @param margin the starting title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginStart
     * @see #getExpandedTitleMarginStart()
     */
    public void setExpandedTitleMarginStart(int margin) {
        expandedMarginStart = margin;
        requestLayout();
    }

    /**
     * @return the top expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginTop
     * @see #setExpandedTitleMarginTop(int)
     */
    public int getExpandedTitleMarginTop() {
        return expandedMarginTop;
    }

    /**
     * Sets the top expanded title margin in pixels.
     *
     * @param margin the top title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginTop
     * @see #getExpandedTitleMarginTop()
     */
    public void setExpandedTitleMarginTop(int margin) {
        expandedMarginTop = margin;
        requestLayout();
    }

    /**
     * @return the ending expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginEnd
     * @see #setExpandedTitleMarginEnd(int)
     */
    public int getExpandedTitleMarginEnd() {
        return expandedMarginEnd;
    }

    /**
     * Sets the ending expanded title margin in pixels.
     *
     * @param margin the ending title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginEnd
     * @see #getExpandedTitleMarginEnd()
     */
    public void setExpandedTitleMarginEnd(int margin) {
        expandedMarginEnd = margin;
        requestLayout();
    }

    /**
     * @return the bottom expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginBottom
     * @see #setExpandedTitleMarginBottom(int)
     */
    public int getExpandedTitleMarginBottom() {
        return expandedMarginBottom;
    }

    /**
     * Sets the bottom expanded title margin in pixels.
     *
     * @param margin the bottom title margin in pixels
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_expandedTitleMarginBottom
     * @see #getExpandedTitleMarginBottom()
     */
    public void setExpandedTitleMarginBottom(int margin) {
        expandedMarginBottom = margin;
        requestLayout();
    }

    /**
     * Set the amount of visible height in pixels used to define when to trigger a scrim visibility
     * change.
     *
     * <p>If the visible height of this view is less than the given value, the scrims will be made
     * visible, otherwise they are hidden.
     *
     * @param height value in pixels used to define when to trigger a scrim visibility change
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_scrimVisibleHeightTrigger
     */
    public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
        if (scrimVisibleHeightTrigger != height) {
            scrimVisibleHeightTrigger = height;
            // Update the scrim visibility
            updateScrimVisibility();
        }
    }

    /**
     * Returns the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     * @see #setScrimVisibleHeightTrigger(int)
     */
    public int getScrimVisibleHeightTrigger() {
        if (scrimVisibleHeightTrigger >= 0) {
            // If we have one explicitly set, return it
            return scrimVisibleHeightTrigger;
        }

        // Otherwise we'll use the default computed value
        final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight > 0) {
            // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
            return Math.min((minHeight * 2) + insetTop, getHeight());
        }

        // If we reach here then we don't have a min height set. Instead we'll take a
        // guess at 1/3 of our height being visible
        return getHeight() / 3;
    }

    /**
     * Set the duration used for scrim visibility animations.
     *
     * @param duration the duration to use in milliseconds
     * @attr ref com.google.android.material.R.styleable#NRCollapsingToolbarLayout_scrimAnimationDuration
     */
    public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
        scrimAnimationDuration = duration;
    }

    /**
     * Returns the duration in milliseconds used for scrim visibility animations.
     */
    public long getScrimAnimationDuration() {
        return scrimAnimationDuration;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

        /**
         * @hide
         */
        @RestrictTo(LIBRARY_GROUP)
        @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX})
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {
        }

        /**
         * The view will act as normal with no collapsing behavior.
         */
        public static final int COLLAPSE_MODE_OFF = 0;

        /**
         * The view will pin in place until it reaches the bottom of the {@link
         * NRCollapsingToolbarLayout}.
         */
        public static final int COLLAPSE_MODE_PIN = 1;

        /**
         * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)} to
         * change the multiplier used.
         */
        public static final int COLLAPSE_MODE_PARALLAX = 2;

        int collapseMode = COLLAPSE_MODE_OFF;
        float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.NRCollapsingToolbarLayout_Layout);
            collapseMode =
                    a.getInt(
                            R.styleable.NRCollapsingToolbarLayout_Layout_nr_layout_collapseMode, COLLAPSE_MODE_OFF);
            setParallaxMultiplier(
                    a.getFloat(
                            R.styleable.NRCollapsingToolbarLayout_Layout_nr_layout_collapseParallaxMultiplier,
                            DEFAULT_PARALLAX_MULTIPLIER));
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(19)
        public LayoutParams(FrameLayout.LayoutParams source) {
            // The copy constructor called here only exists on API 19+.
            super(source);
        }

        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or {@link
         *                     #COLLAPSE_MODE_PARALLAX}.
         */
        public void setCollapseMode(@CollapseMode int collapseMode) {
            this.collapseMode = collapseMode;
        }

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or
         * {@link #COLLAPSE_MODE_PARALLAX}.
         */
        @CollapseMode
        public int getCollapseMode() {
            return collapseMode;
        }

        /**
         * Set the parallax scroll multiplier used in conjunction with {@link #COLLAPSE_MODE_PARALLAX}.
         * A value of {@code 0.0} indicates no movement at all, {@code 1.0f} indicates normal scroll
         * movement.
         *
         * @param multiplier the multiplier.
         * @see #getParallaxMultiplier()
         */
        public void setParallaxMultiplier(float multiplier) {
            parallaxMult = multiplier;
        }

        /**
         * Returns the parallax scroll multiplier used in conjunction with {@link
         * #COLLAPSE_MODE_PARALLAX}.
         *
         * @see #setParallaxMultiplier(float)
         */
        public float getParallaxMultiplier() {
            return parallaxMult;
        }
    }

    /**
     * Show or hide the scrims if needed
     */
    final void updateScrimVisibility() {
        if (contentScrim != null || statusBarScrim != null) {
            setScrimsShown(getHeight() + currentOffset < getScrimVisibleHeightTrigger());
        }
    }

    final int getMaxOffsetForPinChild(View child) {
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return getHeight() - offsetHelper.getLayoutTop() - child.getHeight() - lp.bottomMargin;
    }

    private void updateContentDescriptionFromTitle() {
        // Set this layout's contentDescription to match the title if it's shown by CollapsingTextHelper
        setContentDescription(getTitle());
    }

    private class OffsetUpdateListener implements RefreshStickyLayout.OffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override
        public void onOffsetChanged(RefreshStickyLayout layout, int verticalOffset) {
            currentOffset = verticalOffset;

            final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.collapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        offsetHelper.setTopAndBottomOffset(
                                MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        if (verticalOffset > 0) {
                            View parent = ((View) layout.getParent());
                            float scale = 1f + 1f * verticalOffset / parent.getHeight();
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                        } else {
                            offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMult));
                        }
                        break;
                    default:
                        break;
                }
            }

            // Show or hide the scrims if needed
            updateScrimVisibility();

            if (statusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(NRCollapsingToolbarLayout.this);
            }
            final int expandRange =
                    getHeight() - ViewCompat.getMinimumHeight(NRCollapsingToolbarLayout.this) - insetTop;
            collapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset > 0 ? 0 : verticalOffset) / (float) expandRange);
        }
    }
}
