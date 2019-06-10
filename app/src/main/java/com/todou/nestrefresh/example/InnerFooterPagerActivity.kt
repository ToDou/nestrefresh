package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.todou.nestrefresh.RefreshStickyLayout

import java.util.Collections
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

        val refreshStickyLayout = findViewById<RefreshStickyLayout>(R.id.view_refresh_sticky_layout)

        refreshStickyLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                Toast.makeText(this@InnerFooterPagerActivity, "Refresh Start", Toast.LENGTH_SHORT).show()
                refreshStickyLayout.postDelayed({
                    Toast.makeText(this@InnerFooterPagerActivity, "Refresh End", Toast.LENGTH_SHORT).show()
                    refreshStickyLayout.setRefresh(false)
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
