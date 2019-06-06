package com.todou.nestrefresh.example

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.todou.nestrefresh.NestRefreshLayout

import java.util.Collections

class PagerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapterTest
    private lateinit var fragmentAdapter: FragmentAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var imageAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nest_refresh_viewpager)

        recyclerView = findViewById(R.id.recycler_view_inner)
        viewPager = findViewById(R.id.view_pager)
        imageAvatar = findViewById(R.id.image_avatar)

        Picasso
            .get()
            .load("https://avatars1.githubusercontent.com/u/7405589?s=460&v=4")
            .into(imageAvatar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapterTest()
        recyclerView.adapter = adapter
        adapter.updateDatas(Collections.nCopies(10, Any()))

        val pullRefreshHoverLayout = findViewById<NestRefreshLayout>(R.id.pull_refresh_hover)

        pullRefreshHoverLayout.setOnRefreshListener(object:NestRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                Toast.makeText(this@PagerActivity, "Refresh Start", Toast.LENGTH_SHORT).show()
                pullRefreshHoverLayout.postDelayed({
                    Toast.makeText(this@PagerActivity, "Refresh End", Toast.LENGTH_SHORT).show()
                    pullRefreshHoverLayout.setRefresh(false)
                    adapter.updateDatas(Collections.nCopies(20, Any()))
                }, 2000)
            }
        })


        val list = arrayListOf<InnerFragment>()
        for (i in 0..4) {
            val f = InnerFragment.newInstance()
            list.add(f)
        }
        fragmentAdapter = FragmentAdapter(supportFragmentManager, list)
        viewPager.adapter = fragmentAdapter
    }

}
