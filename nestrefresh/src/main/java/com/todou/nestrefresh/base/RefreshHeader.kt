package com.todou.nestrefresh.base


interface RefreshHeader {
    fun setRefreshCallback(callback: RefreshCallback)

    fun setRefreshEnable(enable: Boolean)

    fun stopRefresh()
}