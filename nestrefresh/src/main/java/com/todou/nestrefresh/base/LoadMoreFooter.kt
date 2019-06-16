package com.todou.nestrefresh.base


interface LoadMoreFooter {
    fun setFooterCallback(callback: LoadMoreFooterCallback)

    fun setShowFooterEnable(showFooterEnable: Boolean)

    fun setHasMore(hasMore: Boolean)

    fun stopLoadMore()
}