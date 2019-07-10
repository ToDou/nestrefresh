package com.todou.nestrefresh.material

import android.graphics.Typeface

/**
 * From material-components-android
 * https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/resources/TextAppearanceFontCallback.java
 */
abstract class TextAppearanceFontCallback {
    /**
     * Called when an asynchronous font was finished loading.
     *
     * @param typeface Font that was loaded.
     * @param fontResolvedSynchronously Whether the font was loaded synchronously, because it was
     * already available.
     */
    abstract fun onFontRetrieved(typeface: Typeface, fontResolvedSynchronously: Boolean)

    /** Called when an asynchronous font failed to load.  */
    abstract fun onFontRetrievalFailed(reason: Int)
}