package com.todou.nestrefresh.material;

import android.graphics.Typeface;

/**
 * From material-components-android
 * https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/resources/TextAppearanceFontCallback.java
 */
public abstract class TextAppearanceFontCallback {
  /**
   * Called when an asynchronous font was finished loading.
   *
   * @param typeface Font that was loaded.
   * @param fontResolvedSynchronously Whether the font was loaded synchronously, because it was
   *     already available.
   */
  public abstract void onFontRetrieved(Typeface typeface, boolean fontResolvedSynchronously);

  /** Called when an asynchronous font failed to load. */
  public abstract void onFontRetrievalFailed(int reason);
}