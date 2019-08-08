package com.todou.nestrefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class RefreshBgView extends View implements CoordinatorLayout.AttachedBehavior{
    private int mDrawBgHeight = 0;
    private int mBgColor;
    private BgDrawable mBgDrawable;

    public RefreshBgView(Context context) {
        this(context, null);
    }

    public RefreshBgView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public RefreshBgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshBgView);
        this.mBgColor = a.getColor(R.styleable.RefreshBgView_refresh_bg_color, Color.WHITE);
        a.recycle();

        mBgDrawable = new BgDrawable(mBgColor);
        mBgDrawable.setMaxBottom(getBottom());
        setBackground(mBgDrawable);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mBgDrawable.setMaxBottom(bottom);
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new Behavior();
    }

    protected static class BaseBehavior<T extends RefreshBgView> extends CoordinatorLayout.Behavior<T>{
        public BaseBehavior() {
        }

        public BaseBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, T child, View dependency) {
            if (dependency instanceof RefreshBarLayout) {
                return true;
            }
            return super.layoutDependsOn(parent, child, dependency);
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, T child, View dependency) {
            if (dependency instanceof RefreshBarLayout) {
                RefreshBarLayout refreshBarLayout = (RefreshBarLayout) dependency;
                int refreshHeaderHeight = refreshBarLayout.getRefreshHeaderOffset();
                int offset = refreshBarLayout.getTopBottomOffset();
                int preHeight = child.getLayoutParams().height;
                int newHeight;
                if (offset > refreshHeaderHeight) {
                    newHeight = offset;
                } else {
                    newHeight = 0;
                }
                if (preHeight != newHeight) {
                    child.updateBgHeight(newHeight);
                }
            }
            return false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        updateBgDrawable();
        super.draw(canvas);
    }

    private void updateBgDrawable() {
        int height = mDrawBgHeight;
        int width = getWidth();
        if (getBackground() != null) {
            getBackground().setBounds(0, 0, width, height);
        }
    }

    public void updateBgHeight(int height) {
        mDrawBgHeight = height;
        invalidate();
    }

    public static class BgDrawable extends ColorDrawable {
        private int mMaxBottom = 0;

        public BgDrawable(int color) {
            super(color);
        }

        public void setMaxBottom(int maxBottom) {
            mMaxBottom = maxBottom;
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            if (bottom >= mMaxBottom) {
                bottom = getBounds().bottom;
            }
            super.setBounds(left, top, right, bottom);
        }
    }

    public static class Behavior extends BaseBehavior<RefreshBgView> {
        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }


}