package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.todou.nestrefresh.LoadMoreFooterView
import com.todou.nestrefresh.NestRefreshLayout
import com.todou.nestrefresh.example.widget.ItemDecoration

import java.util.Collections

class RefreshSingleActivity : AppCompatActivity() {

    private lateinit var recyclerViewScroll: RecyclerView
    private lateinit var recyclerAdapterScroll: RecyclerAdapterScroll
    private lateinit var loadMoreFooterView: LoadMoreFooterView

    private var currentPage = 1
    private var initPage = 1
    private var maxPage = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_single_refresh)
        loadMoreFooterView = findViewById(R.id.view_footer)

        recyclerViewScroll = findViewById(R.id.recycler_view)
        recyclerViewScroll.layoutManager = LinearLayoutManager(this)
        recyclerAdapterScroll = RecyclerAdapterScroll()
        recyclerViewScroll.adapter = recyclerAdapterScroll
        recyclerViewScroll.addItemDecoration(
            ItemDecoration(
                this, RecyclerView.VERTICAL
                , resources.getDimensionPixelSize(R.dimen.margin_normal)
            )
        )
        recyclerAdapterScroll.updateDatas(Collections.nCopies(20, Any()))


//        val pullRefreshHoverLayout = findViewById<NestRefreshLayout>(R.id.pull_refresh_hover)
//
//        pullRefreshHoverLayout.setOnRefreshListener(object : NestRefreshLayout.OnRefreshListener {
//            override fun onRefresh() {
//                pullRefreshHoverLayout.postDelayed({
//                    pullRefreshHoverLayout.setRefresh(false)
//                    currentPage = initPage
//                    loadMoreFooterView.setHasMore(currentPage <= maxPage)
//                }, 2000)
//            }
//        })

        loadMoreFooterView.setOnLoadMoreListener(object : LoadMoreFooterView.OnLoadMoreListener {
            override fun onLoadMore() {
                loadMoreFooterView.postDelayed({
                    loadMoreFooterView.setIsLoadMore(false)
                    currentPage++
                    loadMoreFooterView.setHasMore(currentPage <= maxPage)
                }, 2000)
            }
        })

    }
}
