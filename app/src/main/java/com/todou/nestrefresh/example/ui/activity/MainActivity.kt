package com.todou.nestrefresh.example.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.todou.nestrefresh.example.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onSingleRecyclerClick(view: View) {
        startActivity(Intent(this, SingleActivity::class.java))
    }

    fun onViewPagerActivityClick(view: View) {
        startActivity(Intent(this, PagerActivity::class.java))
    }

    fun onRefreshSingleActivityClick(view: View) {
        startActivity(Intent(this, RefreshSingleActivity::class.java))
    }

    fun onInnerFooterPagerActivityClick(view: View) {
        startActivity(Intent(this, InnerFooterPagerActivity::class.java))
    }

    fun onAppBarLayoutRefreshClick(view: View) {
        startActivity(Intent(this, AppBarRefreshActivity::class.java))
    }

    fun onRefreshBarLayoutClick(view: View) {
        startActivity(Intent(this, RefreshBarLayoutActivity::class.java))
    }
}
