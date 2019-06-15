package com.todou.nestrefresh.material.resources;

import android.graphics.Typeface;
import com.todou.nestrefresh.material.TextAppearanceFontCallback;

public final class NRCancelableFontCallback extends TextAppearanceFontCallback {

  /** Functional interface for method to call when font is retrieved (or fails with fallback). */
  public interface ApplyFont {
    void apply(Typeface font);
  }

  private final Typeface fallbackFont;
  private final ApplyFont applyFont;
  private boolean cancelled;

  public NRCancelableFontCallback(ApplyFont applyFont, Typeface fallbackFont) {
    this.fallbackFont = fallbackFont;
    this.applyFont = applyFont;
  }

  @Override
  public void onFontRetrieved(Typeface font, boolean fontResolvedSynchronously) {
    updateIfNotCancelled(font);
  }

  @Override
  public void onFontRetrievalFailed(int reason) {
    updateIfNotCancelled(fallbackFont);
  }

  /**
   * Cancels this callback. No async operations will actually be interrupted as a result of this
   * method, but it will ignore any subsequent result of the fetch.
   *
   * <p>Callback cannot be resumed after canceling. New callback has to be created.
   */
  public void cancel() {
    cancelled = true;
  }

  private void updateIfNotCancelled(Typeface updatedFont) {
    if (!cancelled) {
      applyFont.apply(updatedFont);
    }
  }
}
