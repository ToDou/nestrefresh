package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.todou.nestrefresh.RefreshStickyLayout

import java.util.Collections
import com.todou.nestrefresh.LoadMoreFooterView
import com.todou.nestrefresh.base.OnRefreshListener


class PagerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapterInHeader
    private lateinit var fragmentAdapter: FragmentAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var loadMoreFooterView: LoadMoreFooterView

    private var currentPage = 1
    private var initPage = 1
    private var maxPage = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_viewpager)

        recyclerView = findViewById(R.id.recycler_view_inner)
        viewPager = findViewById(R.id.view_pager)
        loadMoreFooterView = findViewById(R.id.view_footer)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterInHeader()
        recyclerView.adapter = adapter
        adapter.updateDatas(Collections.nCopies(5, Any()))

        val refreshStickyLayout = findViewById<RefreshStickyLayout>(R.id.pull_refresh_hover)

        refreshStickyLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                Toast.makeText(this@PagerActivity, "Refresh Start", Toast.LENGTH_SHORT).show()
                refreshStickyLayout.postDelayed({
                    Toast.makeText(this@PagerActivity, "Refresh End", Toast.LENGTH_SHORT).show()
                    refreshStickyLayout.setRefresh(false)
                    currentPage = initPage
                    loadMoreFooterView.setHasMore(currentPage <= maxPage)
                }, 2000)
            }
        })


        val list = arrayListOf<RecyclerFragment>()
        for (i in 0..4) {
            val f = RecyclerFragment.newInstance()
            list.add(f)
        }
        fragmentAdapter = FragmentAdapter(supportFragmentManager, list)
        viewPager.adapter = fragmentAdapter

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
