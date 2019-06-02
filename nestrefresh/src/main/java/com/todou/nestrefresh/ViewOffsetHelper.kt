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

import android.os.Build
import android.support.v4.view.ViewCompat
import android.view.View

/**
 * Utility helper for moving a [View] around using
 * [View.offsetLeftAndRight] and
 * [View.offsetTopAndBottom].
 *
 *
 * Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
internal class ViewOffsetHelper(private val view: View) {

    private var layoutTop: Int = 0
    private var layoutLeft: Int = 0
    private var offsetTop: Int = 0
    private var offsetLeft: Int = 0

    fun onViewLayout() {
        layoutTop = view.top
        layoutLeft = view.left

        updateOffsets()
    }

    private fun updateOffsets() {
        ViewCompat.offsetTopAndBottom(view, offsetTop - (view.top - layoutTop))
        ViewCompat.offsetLeftAndRight(view, offsetLeft - (view.left - layoutLeft))

        if (Build.VERSION.SDK_INT < 23) {
            tickleInvalidationFlag(view)
            val vp = view.parent
            if (vp is View) {
                tickleInvalidationFlag(vp as View)
            }
        }
    }

    private fun tickleInvalidationFlag(view: View) {
        val y = view.translationY
        view.translationY = y + 1.0f
        view.translationY = y
    }

    fun setTopAndBottomOffset(offset: Int): Boolean {
        if (offsetTop != offset) {
            offsetTop = offset
            updateOffsets()
            return true
        }
        return false
    }

    fun setLeftAndRightOffset(offset: Int): Boolean {
        if (offsetLeft != offset) {
            offsetLeft = offset
            updateOffsets()
            return true
        }
        return false
    }

    fun getTopAndBottomOffset(): Int {
        return offsetTop
    }

    fun getLeftAndRightOffset(): Int {
        return offsetLeft
    }
}