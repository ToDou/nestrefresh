package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.todou.nestrefresh.RefreshStickyLayout
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.widget.ItemDecoration

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


        val pullRefreshHoverLayout = findViewById<RefreshStickyLayout>(R.id.pull_refresh_hover)

        pullRefreshHoverLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                pullRefreshHoverLayout.postDelayed({
                    pullRefreshHoverLayout.setRefresh(false)
                }, 2000)
            }
        })

    }
}
