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
import android.support.v4.util.ObjectsCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat


class RefreshBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), RefreshCallback, CoordinatorLayout.AttachedBehavior {

    var childScrollAbleList: List<View> = ArrayList()
        private set
    private var refreshBarBehavior: RefreshBarBehavior? = null
    private var onRefreshListener: OnRefreshListener? = null
    private lateinit var headerView: View
    private var listeners: MutableList<OffsetChangedListener> = mutableListOf()
    internal var lastInsets: WindowInsetsCompat? = null

    init {
        orientation = VERTICAL
        ViewCompat.setOnApplyWindowInsetsListener(
            this
        ) { v, insets -> onWindowInsetChanged(insets) }
    }

    private fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null

        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets
        }

        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets
            requestLayout()
        }
        return insets
    }

    val stickyHeight: Int
        get() {
            var result = 0
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val layoutParams = view.layoutParams as LayoutParams
                if (layoutParams.scrollFlags == LayoutParams.SCROLL_FLAG_STICKY) {
                    result += view.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                }

                if (layoutParams.scrollFlags == LayoutParams.SCROLL_FLAG_COLLAPSE) {
                    result += view.minimumHeight
                }
            }
            result += getTopInset()
            return result
        }

    fun getPinHeightWithoutInsetTop(): Int {
        return stickyHeight - getTopInset()
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

    private fun getTopInset(): Int {
        return lastInsets?.systemWindowInsetTop ?: 0
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
        refreshBarBehavior?.setState(
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
        val behavior = RefreshBarBehavior()
        behavior.setSpringHeaderCallback(this)
        refreshBarBehavior = behavior
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
            val a = c.obtainStyledAttributes(attrs, R.styleable.RefreshBarLayout_Layout)
            this.scrollFlags = a.getInt(R.styleable.RefreshBarLayout_Layout_nr_layout_scrollFlags, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: MarginLayoutParams) : super(source)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        companion object {
            const val SCROLL_FLAG_STICKY = 0x1
            const val SCROLL_FLAG_REFRESH_HEADER = 0x2
            const val SCROLL_FLAG_COLLAPSE = 0x4
        }
    }

    interface OffsetChangedListener {
        fun onOffsetChanged(refreshBarLayout: RefreshBarLayout, verticalOffset: Int)
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

        if (listeners != null) {
            listeners.forEach {
                it.onOffsetChanged(this, offset)
            }
        }
    }
}
