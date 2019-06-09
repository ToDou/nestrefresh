package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.todou.nestrefresh.NestRefreshLayout

import java.util.Collections
import com.todou.nestrefresh.LoadMoreFooterView
import com.todou.nestrefresh.base.OnRefreshListener


class InnerFooterPagerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapterInHeader
    private lateinit var fragmentAdapter: FragmentAdapter
    private lateinit var viewPager: ViewPager

    private var currentPage = 1
    private var initPage = 1
    private var maxPage = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_viewpager_inner_footer)

        recyclerView = findViewById(R.id.recycler_view_inner)
        viewPager = findViewById(R.id.view_pager)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterInHeader()
        recyclerView.adapter = adapter
        adapter.updateDatas(Collections.nCopies(5, Any()))

        val pullRefreshHoverLayout = findViewById<NestRefreshLayout>(R.id.pull_refresh_hover)

        pullRefreshHoverLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                Toast.makeText(this@InnerFooterPagerActivity, "Refresh Start", Toast.LENGTH_SHORT).show()
                pullRefreshHoverLayout.postDelayed({
                    Toast.makeText(this@InnerFooterPagerActivity, "Refresh End", Toast.LENGTH_SHORT).show()
                    pullRefreshHoverLayout.setRefresh(false)
                    currentPage = initPage
                }, 2000)
            }
        })


        val list = arrayListOf<Fragment>()
        for (i in 0..4) {
            val f = InnerFooterFragment.newInstance()
            list.add(f)
        }
        fragmentAdapter = FragmentAdapter(supportFragmentManager, list)
        viewPager.adapter = fragmentAdapter

    }

}
