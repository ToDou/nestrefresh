package com.todou.nestrefresh.example

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class FragmentAdapter(fm: FragmentManager, private val fmList: List<Fragment>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return fmList[position]
    }

    override fun getCount(): Int {
        return fmList.size
    }
}