package com.todou.nestrefresh

import android.content.Context
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.NestedScrollingChild
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.base.RefreshCallback
import com.todou.nestrefresh.base.RefreshHeaderBehavior

import java.util.ArrayList
import android.support.design.widget.AppBarLayout.BaseOnOffsetChangedListener
import android.support.v4.view.ViewCompat




class RefreshStickyLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), RefreshCallback, CoordinatorLayout.AttachedBehavior {

    var childScrollAbleList: List<View> = ArrayList()
        private set
    private var refreshStickyBehavior: RefreshStickyBehavior? = null
    private var onRefreshListener: OnRefreshListener? = null
    private lateinit var headerView: View
    private var listeners: MutableList<OffsetChangedListener> = mutableListOf()

    init {
        orientation = VERTICAL
    }

    val stickyHeight: Int
        get() {
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val layoutParams = view.layoutParams as LayoutParams
                if (layoutParams.scrollFlags and LayoutParams.SCROLL_FLAG_STICKY != 0) {
                    return view.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                }
            }
            return 0
        }

    val refreshHeaderOffset: Int
        get() {
            if (!this::headerView.isInitialized) {
                for (i in 0 until childCount) {
                    val view = getChildAt(i)
                    val layoutParams = view.layoutParams as LayoutParams
                    if (layoutParams.scrollFlags and LayoutParams.SCROLL_FLAG_REFRESH_HEADER != 0) {
                        headerView = view
                        break
                    }
                }
            }
            val params = headerView.layoutParams as LayoutParams
            return headerView.measuredHeight + params.topMargin + params.bottomMargin
        }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateAllChildList()
    }

    private fun updateAllChildList() {
        childScrollAbleList = getScrollableChild(this)
    }

    private fun getScrollableChild(viewGroup: ViewGroup): List<View> {
        val result = ArrayList<View>()
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            view.takeIf { it is NestedScrollingChild && (it as NestedScrollingChild).isNestedScrollingEnabled }
                ?.let {
                    if (it is RecyclerView && it.layoutManager is LinearLayoutManager) {
                        val manager = it.layoutManager as LinearLayoutManager
                        if (manager.canScrollVertically()) {
                            result.add(view)
                        }
                    } else if (it is NestedScrollView){
                        result.add(view)
                    }
                }
            if (view is ViewGroup) {
                result.addAll(getScrollableChild(view))
            }
        }
        return result
    }

    override fun onScroll(offset: Int, fraction: Float, nextState: Int) {
        onScrollToRefreshHeader(offset, fraction, nextState)
    }

    override fun onStateChanged(newState: Int) {
        onStateChangedToRefreshHeader(newState)
        if (newState == RefreshHeaderBehavior.STATE_HOVERING) {
            onRefreshListener?.onRefresh()
        }
    }

    private fun onScrollToRefreshHeader(offset: Int, fraction: Float, nextState: Int) {
        if (headerView is RefreshCallback) {
            (headerView as RefreshCallback).onScroll(offset, fraction, nextState)
        }
    }

    private fun onStateChangedToRefreshHeader(newState: Int) {
        if (headerView is RefreshCallback) {
            (headerView as RefreshCallback).onStateChanged(newState)
        }
    }

    fun setRefresh(refreshing: Boolean) {
        refreshStickyBehavior?.setState(
            if (refreshing)
                RefreshHeaderBehavior.STATE_HOVERING
            else
                RefreshHeaderBehavior.STATE_COLLAPSED
        )
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        val behavior = RefreshStickyBehavior()
        behavior.setSpringHeaderCallback(this)
        refreshStickyBehavior = behavior
        return behavior
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LinearLayout.LayoutParams {
        return LayoutParams(-1, -2)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LinearLayout.LayoutParams {
        return LayoutParams(this.context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): LinearLayout.LayoutParams {
        return if (Build.VERSION.SDK_INT >= 19 && lp is LinearLayout.LayoutParams) {
            LayoutParams(lp)
        } else {
            (lp as? MarginLayoutParams)?.let { LayoutParams(it) } ?: LayoutParams(lp)
        }
    }

    class LayoutParams : LinearLayout.LayoutParams {

        var scrollFlags: Int = 0

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.NestRefreshLayout_Layout)
            this.scrollFlags = a.getInt(R.styleable.NestRefreshLayout_Layout_nest_refresh_layout_scrollFlags, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: MarginLayoutParams) : super(source)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        companion object {
            const val SCROLL_FLAG_STICKY = 0x1
            const val SCROLL_FLAG_REFRESH_HEADER = 0x2
        }
    }

    interface OffsetChangedListener {
        fun onOffsetChanged(refreshStickyLayout: RefreshStickyLayout, verticalOffset: Int)
    }

    fun addOnOffsetChangedListener(listener: OffsetChangedListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeOnOffsetChangedListener(listener: OffsetChangedListener) {
        this.listeners.remove(listener)
    }

    fun onOffsetChanged(offset: Int) {
        if (!willNotDraw()) {
            ViewCompat.postInvalidateOnAnimation(this)
        }

        // Iterate backwards through the list so that most recently added listeners
        // get the first chance to decide
        if (listeners != null) {
            var i = 0
            val z = listeners.size
            while (i < z) {
                val listener = listeners[i]
                listener?.onOffsetChanged(this, offset)
                i++
            }
        }
    }
}
