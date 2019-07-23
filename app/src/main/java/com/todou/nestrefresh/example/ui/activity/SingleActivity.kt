package com.todou.nestrefresh.example.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.R
import com.todou.nestrefresh.example.ui.adapter.RecyclerAdapterInHeader
import com.todou.nestrefresh.example.ui.adapter.RecyclerAdapterScroll
import com.todou.nestrefresh.example.ui.widget.ItemDecoration
import kotlinx.android.synthetic.main.activity_nest_refresh_recycler.*

import java.util.Collections

class SingleActivity : AppCompatActivity() {

    private lateinit var adapter: RecyclerAdapterInHeader
    private lateinit var recyclerAdapterScroll: RecyclerAdapterScroll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_recycler)

        recycler_view_inner.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterInHeader()
        recycler_view_inner.adapter = adapter
        adapter.updateDatas(Collections.nCopies(5, Any()))

        recycler_view.layoutManager = LinearLayoutManager(this)
        recyclerAdapterScroll = RecyclerAdapterScroll()
        recycler_view.adapter = recyclerAdapterScroll
        recycler_view.addItemDecoration(
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
