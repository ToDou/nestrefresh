package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.todou.nestrefresh.LoadMoreFooterView
import com.todou.nestrefresh.base.OnRefreshListener
import kotlinx.android.synthetic.main.activity_appbarlayout_refresh.*
import kotlinx.android.synthetic.main.activity_appbarlayout_refresh.view_footer
import kotlinx.android.synthetic.main.activity_appbarlayout_refresh.view_refresh_header

class AppBarRefreshActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appbarlayout_refresh)

        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                view_refresh_header.postDelayed({
                    view_refresh_header.setRefresh(false)
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