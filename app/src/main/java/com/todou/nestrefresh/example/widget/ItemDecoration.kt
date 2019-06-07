package com.todou.nestrefresh.example.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.View


class ItemDecoration(context: Context?, orientation: Int) : DividerItemDecoration(context, orientation) {
    private var mDividerHeight: Int = 0
    private var tOrientation: Int = orientation

    constructor(context: Context?, orientation: Int, dividerHeight: Int) : this(context, orientation) {
        mDividerHeight = dividerHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (mDividerHeight > 0) {
            if (tOrientation == 1) {
                outRect.set(0, 0, 0, mDividerHeight)
            } else {
                outRect.set(0, 0, mDividerHeight, 0)
            }
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}