package com.todou.nestrefresh.material.resources;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.TypedValue;

public class NRTintTypedArray {
    private final Context mContext;
    private final TypedArray mWrapped;
    private TypedValue mTypedValue;

    public static NRTintTypedArray obtainStyledAttributes(Context context, AttributeSet set, int[] attrs) {
        return new NRTintTypedArray(context, context.obtainStyledAttributes(set, attrs));
    }

    public static NRTintTypedArray obtainStyledAttributes(Context context, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
        return new NRTintTypedArray(context, context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes));
    }

    public static NRTintTypedArray obtainStyledAttributes(Context context, int resid, int[] attrs) {
        return new NRTintTypedArray(context, context.obtainStyledAttributes(resid, attrs));
    }

    private NRTintTypedArray(Context context, TypedArray array) {
        this.mContext = context;
        this.mWrapped = array;
    }

    public Drawable getDrawable(int index) {
        if (this.mWrapped.hasValue(index)) {
            int resourceId = this.mWrapped.getResourceId(index, 0);
            if (resourceId != 0) {
                return AppCompatResources.getDrawable(this.mContext, resourceId);
            }
        }

        return this.mWrapped.getDrawable(index);
    }

    public int length() {
        return this.mWrapped.length();
    }

    public int getIndexCount() {
        return this.mWrapped.getIndexCount();
    }

    public int getIndex(int at) {
        return this.mWrapped.getIndex(at);
    }

    public Resources getResources() {
        return this.mWrapped.getResources();
    }

    public CharSequence getText(int index) {
        return this.mWrapped.getText(index);
    }

    public String getString(int index) {
        return this.mWrapped.getString(index);
    }

    public String getNonResourceString(int index) {
        return this.mWrapped.getNonResourceString(index);
    }

    public boolean getBoolean(int index, boolean defValue) {
        return this.mWrapped.getBoolean(index, defValue);
    }

    public int getInt(int index, int defValue) {
        return this.mWrapped.getInt(index, defValue);
    }

    public float getFloat(int index, float defValue) {
        return this.mWrapped.getFloat(index, defValue);
    }

    public int getColor(int index, int defValue) {
        return this.mWrapped.getColor(index, defValue);
    }

    public ColorStateList getColorStateList(int index) {
        if (this.mWrapped.hasValue(index)) {
            int resourceId = this.mWrapped.getResourceId(index, 0);
            if (resourceId != 0) {
                ColorStateList value = AppCompatResources.getColorStateList(this.mContext, resourceId);
                if (value != null) {
                    return value;
                }
            }
        }

        return this.mWrapped.getColorStateList(index);
    }

    public int getInteger(int index, int defValue) {
        return this.mWrapped.getInteger(index, defValue);
    }

    public float getDimension(int index, float defValue) {
        return this.mWrapped.getDimension(index, defValue);
    }

    public int getDimensionPixelOffset(int index, int defValue) {
        return this.mWrapped.getDimensionPixelOffset(index, defValue);
    }

    public int getDimensionPixelSize(int index, int defValue) {
        return this.mWrapped.getDimensionPixelSize(index, defValue);
    }

    public int getLayoutDimension(int index, String name) {
        return this.mWrapped.getLayoutDimension(index, name);
    }

    public int getLayoutDimension(int index, int defValue) {
        return this.mWrapped.getLayoutDimension(index, defValue);
    }

    public float getFraction(int index, int base, int pbase, float defValue) {
        return this.mWrapped.getFraction(index, base, pbase, defValue);
    }

    public int getResourceId(int index, int defValue) {
        return this.mWrapped.getResourceId(index, defValue);
    }

    public CharSequence[] getTextArray(int index) {
        return this.mWrapped.getTextArray(index);
    }

    public boolean getValue(int index, TypedValue outValue) {
        return this.mWrapped.getValue(index, outValue);
    }

    public int getType(int index) {
        if (Build.VERSION.SDK_INT >= 21) {
            return this.mWrapped.getType(index);
        } else {
            if (this.mTypedValue == null) {
                this.mTypedValue = new TypedValue();
            }

            this.mWrapped.getValue(index, this.mTypedValue);
            return this.mTypedValue.type;
        }
    }

    public boolean hasValue(int index) {
        return this.mWrapped.hasValue(index);
    }

    public TypedValue peekValue(int index) {
        return this.mWrapped.peekValue(index);
    }

    public String getPositionDescription() {
        return this.mWrapped.getPositionDescription();
    }

    public void recycle() {
        this.mWrapped.recycle();
    }

    @RequiresApi(21)
    public int getChangingConfigurations() {
        return this.mWrapped.getChangingConfigurations();
    }
}