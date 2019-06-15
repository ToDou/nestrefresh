package com.todou.nestrefresh.material.animation;

import android.animation.TimeInterpolator;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class NRAnimationUtils {
    public static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    public static final TimeInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final TimeInterpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    public static final TimeInterpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();
    public static final TimeInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public NRAnimationUtils() {
    }

    public static float lerp(float startValue, float endValue, float fraction) {
        return startValue + fraction * (endValue - startValue);
    }

    public static int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (float)(endValue - startValue));
    }
}