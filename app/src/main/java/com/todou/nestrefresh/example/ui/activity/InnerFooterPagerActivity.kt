package com.todou.nestrefresh.example.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast

import java.util.Collections
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.ui.adapter.FragmentAdapter
import com.todou.nestrefresh.example.ui.fragment.InnerFooterFragment
import com.todou.nestrefresh.example.R
import com.todou.nestrefresh.example.ui.adapter.RecyclerAdapterInHeader
import kotlinx.android.synthetic.main.activity_nest_refresh_viewpager_inner_footer.*


class InnerFooterPagerActivity : AppCompatActivity() {

    private lateinit var adapter: RecyclerAdapterInHeader
    private lateinit var fragmentAdapter: FragmentAdapter

    private var currentPage = 1
    private var initPage = 1
    private var maxPage = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_viewpager_inner_footer)

        recycler_view_inner.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterInHeader()
        recycler_view_inner.adapter = adapter
        adapter.updateDatas(Collections.nCopies(5, Any()))

        view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                Toast.makeText(this@InnerFooterPagerActivity, "Refresh Start", Toast.LENGTH_SHORT).show()
                view_refresh_header.postDelayed({
                    Toast.makeText(this@InnerFooterPagerActivity, "Refresh End", Toast.LENGTH_SHORT).show()
                    view_refresh_header.stopRefresh()
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
        view_pager.adapter = fragmentAdapter

    }

}
