package com.todou.nestrefresh.example.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.todou.nestrefresh.base.OnLoadMoreListener
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.R
import com.todou.nestrefresh.example.ui.adapter.RecyclerAdapterScroll
import com.todou.nestrefresh.example.ui.widget.ItemDecoration
import kotlinx.android.synthetic.main.activity_nest_refresh_single_refresh.*

import java.util.Collections

class RefreshSingleActivity : AppCompatActivity() {

    private var currentPage = 1
    private var initPage = 1
    private var maxPage = 3

    private lateinit var adapter : RecyclerAdapterScroll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_single_refresh)

        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterScroll()
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(
            ItemDecoration(
                this, RecyclerView.VERTICAL
                , resources.getDimensionPixelSize(R.dimen.margin_normal)
            )
        )
        adapter.updateDatas(Collections.nCopies(20, Any()))

        view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                view_refresh_header.postDelayed({
                    view_refresh_header.stopRefresh()
                    currentPage = initPage
                    view_footer.setHasMore(currentPage <= maxPage)
                }, 2000)
            }
        })

        view_footer.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                view_footer.postDelayed({
                    view_footer.stopLoadMore()
                    currentPage++
                    view_footer.setHasMore(currentPage <= maxPage)
                }, 2000)
            }
        })

    }
}
