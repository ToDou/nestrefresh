/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.todou.nestrefresh

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

/**
 * Behavior will automatically sets up a [ViewOffsetHelper] on a [View].
 */
open class ViewOffsetBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    private lateinit var viewOffsetHelper: ViewOffsetHelper

    private var tempTopBottomOffset = 0
    private var tempLeftRightOffset = 0

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        this.layoutChild(parent, child, layoutDirection)

        if (!this::viewOffsetHelper.isInitialized) {
            viewOffsetHelper = ViewOffsetHelper(child)
        }
        viewOffsetHelper.onViewLayout()

        if (tempTopBottomOffset != 0) {
            viewOffsetHelper.setTopAndBottomOffset(tempTopBottomOffset)
            tempTopBottomOffset = 0
        }
        if (tempLeftRightOffset != 0) {
            viewOffsetHelper.setLeftAndRightOffset(tempLeftRightOffset)
            tempLeftRightOffset = 0
        }

        return true
    }

    protected open fun layoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int) {
        parent.onLayoutChild(child, layoutDirection)
    }

    open fun setTopAndBottomOffset(offset: Int): Boolean {
        if (this::viewOffsetHelper.isInitialized) {
            return viewOffsetHelper.setTopAndBottomOffset(offset)
        } else {
            tempTopBottomOffset = offset
        }
        return false
    }

    fun setLeftAndRightOffset(offset: Int): Boolean {
        if (this::viewOffsetHelper.isInitialized) {
            return viewOffsetHelper.setLeftAndRightOffset(offset)
        } else {
            tempLeftRightOffset = offset
        }
        return false
    }


    fun getTopAndBottomOffset() : Int{
        return viewOffsetHelper.getTopAndBottomOffset()
    }

    fun getLeftAndRightOffset() : Int{
        return viewOffsetHelper.getLeftAndRightOffset()
    }

}