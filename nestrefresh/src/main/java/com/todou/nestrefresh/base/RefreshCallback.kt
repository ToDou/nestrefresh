package com.todou.nestrefresh.base

interface RefreshCallback {
    fun onScroll(offset: Int, fraction: Float, nextState: Int)

    fun onStateChanged(newState: Int)
}