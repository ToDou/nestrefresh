package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.todou.nestrefresh.LoadMoreFooterView
import com.todou.nestrefresh.base.OnRefreshListener
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

        app_bar.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                app_bar.postDelayed({
                    app_bar .setRefresh(false)
                }, 2000)
            }
        })

        view_footer.setOnLoadMoreListener(object : LoadMoreFooterView.OnLoadMoreListener {
            override fun onLoadMore() {
                view_footer.postDelayed({
                    view_footer.setIsLoadMore(false)
                }, 2000)
            }
        })
    }

}