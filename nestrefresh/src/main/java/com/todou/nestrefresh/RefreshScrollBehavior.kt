package com.todou.nestrefresh

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.todou.nestrefresh.base.BaseBehavior

class RefreshScrollBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null)
    : BaseBehavior<View>(context, attrs) {

    private var loadMoreBehavior: LoadMoreBehavior? = null
    private var refreshBehavior: RefreshBehavior? = null

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val behavior = getBehavior(dependency) ?: return false
        return behavior is LoadMoreBehavior
                || behavior is RefreshBehavior
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val behavior = getBehavior(dependency)
        var offset = 0
        if (behavior is RefreshBehavior) {
            refreshBehavior = behavior
            offset = behavior.currentRange()
        }
        if (behavior is LoadMoreBehavior) {
            loadMoreBehavior = behavior
            offset = behavior.currentRange() + getHeaderOffsetByBehavior()
        }
        return setTopAndBottomOffset(offset)
    }

    private fun getHeaderOffsetByBehavior(): Int {
        return refreshBehavior?.getTopAndBottomOffset() ?: 0
    }
}
