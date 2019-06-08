package com.todou.nestrefresh;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.todou.nestrefresh.base.LoadMoreFooterCallback;

import static com.todou.nestrefresh.LoadMoreFooterBehavior.*;

public class LoadMoreFooter extends LinearLayout implements LoadMoreFooterCallback {

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private ImageView mImageRefreshIndicator;
    private View mViewProgress;
    private TextView mTextDes;

    private CharSequence mTextBelowThreshold;
    private CharSequence mTextAboveThreshold;
    private CharSequence mTextRefreshing;
    private CharSequence mTextNoMore;

    private LoadMoreFooterBehavior mBehavior;
    private OnLoadMoreListener mOnLoadMoreListener;

    private boolean mBelowThreshold = true;
    private int mState;

    public LoadMoreFooter(Context context) {
        this(context, null);
    }

    public LoadMoreFooter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreFooter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) lp).getBehavior();
            if (behavior instanceof LoadMoreFooterBehavior) {
                mBehavior = (LoadMoreFooterBehavior) behavior;
                mBehavior.setFooterCallback(this);
                mBehavior.setShowFooterEnable(isEnabled());
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mBehavior != null) {
            mBehavior.setShowFooterEnable(enabled);
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.view_nest_load_more_footer, this);

        mImageRefreshIndicator = findViewById(R.id.image_refresh);
        mViewProgress = findViewById(R.id.progress_loading);
        mTextDes = findViewById(R.id.text_refresh);

        mTextAboveThreshold = getResources().getString(R.string.nest_refresh_load_more_pull_label);
        mTextBelowThreshold = getResources().getString(R.string.nest_refresh_load_more_release_label);
        mTextRefreshing = getResources().getString(R.string.nest_refresh_load_more_refreshing_label);
        mTextNoMore = getResources().getString(R.string.nest_refresh_load_more_no_more);

        initAnimation();
    }

    private void initAnimation() {
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, -360,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);
    }

    @Override
    public void onScroll(int offset, float fraction, int nextState, boolean hasMore) {
        if (hasMore) {
            doHasMoreScroll(offset, fraction, nextState);
        } else {
            doNoMoreScroll(offset, fraction, nextState);
        }
    }

    private void doNoMoreScroll(int offset, float fraction, int nextState) {
        if (mState != nextState) {
            mState = nextState;
            mTextDes.setText(mTextNoMore);
            mViewProgress.setVisibility(GONE);
            mImageRefreshIndicator.clearAnimation();
            mImageRefreshIndicator.setVisibility(GONE);
        }
    }

    private void doHasMoreScroll(int offset, float fraction, int nextState) {
        if (mState != nextState) {
            if (mState == STATE_COLLAPSED) {
                mImageRefreshIndicator.clearAnimation();
                mImageRefreshIndicator.setRotation(0);
            }
            mState = nextState;
            if (nextState == STATE_DRAGGING) {
                if (mViewProgress.getVisibility() == VISIBLE) {
                    mViewProgress.setVisibility(GONE);
                }

                if (mImageRefreshIndicator.getVisibility() != VISIBLE) {
                    mImageRefreshIndicator.setVisibility(VISIBLE);
                }
                mTextDes.setText(mBelowThreshold ? mTextBelowThreshold : mTextAboveThreshold);
            }
        }

        boolean belowThreshold = fraction < -1;
        if (belowThreshold != mBelowThreshold && nextState != STATE_SETTLING) {
            mBelowThreshold = belowThreshold;
            updateTextAndImage();
        }
    }

    private void updateTextAndImage() {
        if (mBelowThreshold) {
            mImageRefreshIndicator.clearAnimation();
            mImageRefreshIndicator.startAnimation(mReverseFlipAnimation);
        } else {
            mImageRefreshIndicator.clearAnimation();
            mImageRefreshIndicator.startAnimation(mFlipAnimation);
        }
        mTextDes.setText(mBelowThreshold ? mTextBelowThreshold : mTextAboveThreshold);
    }

    @Override
    public void onStateChanged(int newState, boolean hasMore) {
        if (hasMore) {
            if (newState == STATE_HOVERING) {
                mTextDes.setText(mTextRefreshing);
                mViewProgress.setVisibility(VISIBLE);
                mImageRefreshIndicator.clearAnimation();
                mImageRefreshIndicator.setVisibility(GONE);
            }
            if (newState == LoadMoreFooterBehavior.STATE_HOVERING) {
                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        } else {
            mTextDes.setText(mTextNoMore);
            mViewProgress.setVisibility(GONE);
            mImageRefreshIndicator.clearAnimation();
            mImageRefreshIndicator.setVisibility(GONE);
        }
    }

    public void setIsLoadMore(boolean isLoadMore) {
        if (mBehavior != null) {
            mBehavior.setState(isLoadMore ? LoadMoreFooterBehavior.STATE_HOVERING
                    : LoadMoreFooterBehavior.STATE_COLLAPSED);
        }
    }

    public void setHasMore(boolean hasMore) {
        if (mBehavior != null) {
            mBehavior.setHasMore(hasMore);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    @Override
    public void updateChildHeight(int height) {

    }
}
