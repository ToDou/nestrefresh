package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.todou.nestrefresh.RefreshBarLayout
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.widget.ItemDecoration
import kotlinx.android.synthetic.main.activity_nest_refresh_single_refresh.*

import java.util.Collections

class SingleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapterInHeader

    private lateinit var recyclerViewScroll: RecyclerView
    private lateinit var recyclerAdapterScroll: RecyclerAdapterScroll


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_recycler)

        recyclerView = findViewById(R.id.recycler_view_inner)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterInHeader()
        recyclerView.adapter = adapter
        adapter.updateDatas(Collections.nCopies(5, Any()))

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

        view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                view_refresh_header.postDelayed({
                    view_refresh_header.stopRefresh()
                }, 2000)
            }
        })

    }
}
