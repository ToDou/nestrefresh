package com.todou.nestrefresh.base

interface LoadMoreFooterCallback {
    fun onScroll(offset: Int, fraction: Float, nextState: Int, hasMore: Boolean)

    fun onStateChanged(newState: Int, hasMore: Boolean)

    fun updateChildHeight(height: Int)
}