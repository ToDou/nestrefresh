package com.todou.nestrefresh.material.resources

import android.graphics.Typeface
import com.todou.nestrefresh.material.TextAppearanceFontCallback

class NRCancelableFontCallback(private val applyFont: ApplyFont, private val fallbackFont: Typeface) :
    TextAppearanceFontCallback() {
    private var cancelled: Boolean = false

    /** Functional interface for method to call when font is retrieved (or fails with fallback).  */
    interface ApplyFont {
        fun apply(font: Typeface)
    }

    override fun onFontRetrieved(font: Typeface, fontResolvedSynchronously: Boolean) {
        updateIfNotCancelled(font)
    }

    override fun onFontRetrievalFailed(reason: Int) {
        updateIfNotCancelled(fallbackFont)
    }

    /**
     * Cancels this callback. No async operations will actually be interrupted as a result of this
     * method, but it will ignore any subsequent result of the fetch.
     *
     *
     * Callback cannot be resumed after canceling. New callback has to be created.
     */
    fun cancel() {
        cancelled = true
    }

    private fun updateIfNotCancelled(updatedFont: Typeface) {
        if (!cancelled) {
            applyFont.apply(updatedFont)
        }
    }
}
