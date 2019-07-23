package com.todou.nestrefresh.material.resources

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import android.util.AttributeSet
import android.util.TypedValue

class NRTintTypedArray private constructor(private val mContext: Context, private val mWrapped: TypedArray) {
    private var mTypedValue: TypedValue? = null

    val indexCount: Int
        get() = this.mWrapped.indexCount

    val resources: Resources
        get() = this.mWrapped.resources

    val positionDescription: String
        get() = this.mWrapped.positionDescription

    val changingConfigurations: Int
        @RequiresApi(21)
        get() = this.mWrapped.changingConfigurations

    fun getDrawable(index: Int): Drawable? {
        if (this.mWrapped.hasValue(index)) {
            val resourceId = this.mWrapped.getResourceId(index, 0)
            if (resourceId != 0) {
                return AppCompatResources.getDrawable(this.mContext, resourceId)
            }
        }

        return this.mWrapped.getDrawable(index)
    }

    fun length(): Int {
        return this.mWrapped.length()
    }

    fun getIndex(at: Int): Int {
        return this.mWrapped.getIndex(at)
    }

    fun getText(index: Int): CharSequence {
        return this.mWrapped.getText(index)
    }

    fun getString(index: Int): String? {
        return this.mWrapped.getString(index)
    }

    fun getNonResourceString(index: Int): String {
        return this.mWrapped.getNonResourceString(index)
    }

    fun getBoolean(index: Int, defValue: Boolean): Boolean {
        return this.mWrapped.getBoolean(index, defValue)
    }

    fun getInt(index: Int, defValue: Int): Int {
        return this.mWrapped.getInt(index, defValue)
    }

    fun getFloat(index: Int, defValue: Float): Float {
        return this.mWrapped.getFloat(index, defValue)
    }

    fun getColor(index: Int, defValue: Int): Int {
        return this.mWrapped.getColor(index, defValue)
    }

    fun getColorStateList(index: Int): ColorStateList? {
        if (this.mWrapped.hasValue(index)) {
            val resourceId = this.mWrapped.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(this.mContext, resourceId)
                if (value != null) {
                    return value
                }
            }
        }

        return this.mWrapped.getColorStateList(index)
    }

    fun getInteger(index: Int, defValue: Int): Int {
        return this.mWrapped.getInteger(index, defValue)
    }

    fun getDimension(index: Int, defValue: Float): Float {
        return this.mWrapped.getDimension(index, defValue)
    }

    fun getDimensionPixelOffset(index: Int, defValue: Int): Int {
        return this.mWrapped.getDimensionPixelOffset(index, defValue)
    }

    fun getDimensionPixelSize(index: Int, defValue: Int): Int {
        return this.mWrapped.getDimensionPixelSize(index, defValue)
    }

    fun getLayoutDimension(index: Int, name: String): Int {
        return this.mWrapped.getLayoutDimension(index, name)
    }

    fun getLayoutDimension(index: Int, defValue: Int): Int {
        return this.mWrapped.getLayoutDimension(index, defValue)
    }

    fun getFraction(index: Int, base: Int, pbase: Int, defValue: Float): Float {
        return this.mWrapped.getFraction(index, base, pbase, defValue)
    }

    fun getResourceId(index: Int, defValue: Int): Int {
        return this.mWrapped.getResourceId(index, defValue)
    }

    fun getTextArray(index: Int): Array<CharSequence> {
        return this.mWrapped.getTextArray(index)
    }

    fun getValue(index: Int, outValue: TypedValue): Boolean {
        return this.mWrapped.getValue(index, outValue)
    }

    fun getType(index: Int): Int {
        if (Build.VERSION.SDK_INT >= 21) {
            return this.mWrapped.getType(index)
        } else {
            if (this.mTypedValue == null) {
                this.mTypedValue = TypedValue()
            }

            this.mWrapped.getValue(index, this.mTypedValue)
            return this.mTypedValue!!.type
        }
    }

    fun hasValue(index: Int): Boolean {
        return this.mWrapped.hasValue(index)
    }

    fun peekValue(index: Int): TypedValue {
        return this.mWrapped.peekValue(index)
    }

    fun recycle() {
        this.mWrapped.recycle()
    }

    companion object {

        fun obtainStyledAttributes(context: Context, set: AttributeSet, attrs: IntArray): NRTintTypedArray {
            return NRTintTypedArray(context, context.obtainStyledAttributes(set, attrs))
        }

        fun obtainStyledAttributes(
            context: Context,
            set: AttributeSet,
            attrs: IntArray,
            defStyleAttr: Int,
            defStyleRes: Int
        ): NRTintTypedArray {
            return NRTintTypedArray(context, context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes))
        }

        fun obtainStyledAttributes(context: Context, resid: Int, attrs: IntArray): NRTintTypedArray {
            return NRTintTypedArray(context, context.obtainStyledAttributes(resid, attrs))
        }
    }
}