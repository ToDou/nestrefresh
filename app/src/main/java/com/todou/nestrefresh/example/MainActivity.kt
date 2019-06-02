package com.todou.nestrefresh.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

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
}
