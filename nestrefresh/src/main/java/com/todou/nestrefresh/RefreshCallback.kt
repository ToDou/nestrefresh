package com.todou.nestrefresh

interface RefreshCallback {
    fun onScroll(offset: Int, fraction: Float, nextState: Int)

    fun onStateChanged(newState: Int)
}