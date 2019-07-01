/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.todou.nestrefresh.material.resources

import android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.annotation.RestrictTo
import android.support.annotation.StyleableRes
import android.support.v7.content.res.AppCompatResources

/**
 * Utility methods to resolve resources for components.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
object NRMaterialResources {

    /**
     * Returns the [ColorStateList] from the given [TypedArray] attributes. The resource
     * can include themeable attributes, regardless of API level.
     */
    fun getColorStateList(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }

        // Reading a single color with getColorStateList() on API 15 and below doesn't always correctly
        // read the value. Instead we'll first try to read the color directly here.
        if (VERSION.SDK_INT <= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            val color = attributes.getColor(index, -1)
            if (color != -1) {
                return ColorStateList.valueOf(color)
            }
        }

        return attributes.getColorStateList(index)
    }

    /**
     * Returns the [ColorStateList] from the given [NRTintTypedArray] attributes. The
     * resource can include themeable attributes, regardless of API level.
     */
    fun getColorStateList(
        context: Context, attributes: NRTintTypedArray, @StyleableRes index: Int
    ): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }

        // Reading a single color with getColorStateList() on API 15 and below doesn't always correctly
        // read the value. Instead we'll first try to read the color directly here.
        if (VERSION.SDK_INT <= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            val color = attributes.getColor(index, -1)
            if (color != -1) {
                return ColorStateList.valueOf(color)
            }
        }

        return attributes.getColorStateList(index)
    }

    /**
     * Returns the drawable object from the given attributes.
     *
     *
     * This method supports inflation of `<vector>` and `<animated-vector>` resources
     * on devices where platform support is not available.
     */
    fun getDrawable(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): Drawable? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getDrawable(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }
        return attributes.getDrawable(index)
    }

    /**
     * Returns a TextAppearanceSpan object from the given attributes.
     *
     *
     * You only need this if you are drawing text manually. Normally, TextView takes care of this.
     */
    fun getTextAppearance(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): NRTextAppearance? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                return NRTextAppearance(context, resourceId)
            }
        }
        return null
    }

    /**
     * Returns the @StyleableRes index that contains value in the attributes array. If both indices
     * contain values, the first given index takes precedence and is returned.
     */
    @StyleableRes
    internal fun getIndexWithValue(attributes: TypedArray, @StyleableRes a: Int, @StyleableRes b: Int): Int {
        return if (attributes.hasValue(a)) {
            a
        } else b
    }
}