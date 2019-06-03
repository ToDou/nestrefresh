package com.todou.nestrefresh.base

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

open class BaseBehavior<V : View> : ViewOffsetBehavior<V> {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    protected fun getBehavior(view: View): CoordinatorLayout.Behavior<*>? {
        var behavior: CoordinatorLayout.Behavior<*>? = null
        if (view.layoutParams is CoordinatorLayout.LayoutParams) {
            val lp = view.layoutParams as CoordinatorLayout.LayoutParams
            behavior = lp.behavior
        } else {
            if (view.parent != null) {
                behavior = getBehavior(view.parent as View)
            }
        }
        return behavior
    }

}
