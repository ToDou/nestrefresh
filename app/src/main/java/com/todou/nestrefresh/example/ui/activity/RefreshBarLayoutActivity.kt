package com.todou.nestrefresh.example.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.todou.nestrefresh.base.OnLoadMoreListener
import com.todou.nestrefresh.base.OnRefreshListener
import com.todou.nestrefresh.example.R
import kotlinx.android.synthetic.main.activity_refreshbarlayout_refresh.*

class RefreshBarLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refreshbarlayout_refresh)

        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                view_refresh_header.postDelayed({
//                    view_refresh_header.stopRefresh()
                }, 2000)
            }
        })

        view_footer.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                view_footer.postDelayed({
//                    view_footer.stopLoadMore()
                }, 2000)
            }
        })
    }

}