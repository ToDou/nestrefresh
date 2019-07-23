package com.todou.nestrefresh.example.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.Collections
import com.todou.nestrefresh.base.OnLoadMoreListener
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.ui.adapter.FragmentAdapter
import com.todou.nestrefresh.example.R
import com.todou.nestrefresh.example.ui.adapter.RecyclerAdapterInHeader
import com.todou.nestrefresh.example.ui.fragment.RecyclerFragment
import kotlinx.android.synthetic.main.activity_nest_refresh_viewpager.*


class PagerActivity : AppCompatActivity() {

    private lateinit var adapter: RecyclerAdapterInHeader
    private lateinit var fragmentAdapter: FragmentAdapter

    private var currentPage = 1
    private var initPage = 1
    private var maxPage = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_viewpager)

        recycler_view_inner.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterInHeader()
        recycler_view_inner.adapter = adapter
        adapter.updateDatas(Collections.nCopies(5, Any()))

        view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                Toast.makeText(this@PagerActivity, "Refresh Start", Toast.LENGTH_SHORT).show()
                view_refresh_header.postDelayed({
                    Toast.makeText(this@PagerActivity, "Refresh End", Toast.LENGTH_SHORT).show()
                    view_refresh_header.stopRefresh()
                    currentPage = initPage
                    view_footer.setHasMore(currentPage <= maxPage)
                }, 2000)
            }
        })


        val list = arrayListOf<RecyclerFragment>()
        for (i in 0..4) {
            val f = RecyclerFragment.newInstance()
            list.add(f)
        }
        fragmentAdapter = FragmentAdapter(supportFragmentManager, list)
        view_pager.adapter = fragmentAdapter

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
