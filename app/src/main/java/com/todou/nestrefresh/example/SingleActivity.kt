package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.todou.nestrefresh.NestRefreshLayout

import java.util.Collections

class SingleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapterTest

    private lateinit var recyclerViewScroll: RecyclerView
    private lateinit var recyclerAdapterScroll: RecyclerAdapterScroll


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_recycler)

        recyclerView = findViewById(R.id.recycler_view_inner)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterTest()
        recyclerView.adapter = adapter
        adapter.updateDatas(Collections.nCopies(10, Any()))

        recyclerViewScroll = findViewById(R.id.recycler_view)
        recyclerViewScroll.layoutManager = LinearLayoutManager(this)
        recyclerAdapterScroll = RecyclerAdapterScroll()
        recyclerViewScroll.adapter = recyclerAdapterScroll
        recyclerAdapterScroll.updateDatas(Collections.nCopies(40, Any()))


        val pullRefreshHoverLayout = findViewById<NestRefreshLayout>(R.id.pull_refresh_hover)

        pullRefreshHoverLayout.setOnRefreshListener(object:NestRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                pullRefreshHoverLayout.postDelayed({
                    pullRefreshHoverLayout.setRefresh(false)
                    adapter.updateDatas(Collections.nCopies(20, Any()))
                }, 2000)
            }
        })

    }
}
