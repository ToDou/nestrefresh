package com.todou.nestrefresh.material.resources;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.*;
import android.support.design.resources.TextAppearanceConfig;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.provider.FontsContractCompat;
import android.text.TextPaint;
import android.util.Log;
import com.todou.nestrefresh.R;
import com.todou.nestrefresh.material.TextAppearanceFontCallback;

public class NRTextAppearance {

  private static final String TAG = "NRTextAppearance";

  // Enums from AppCompatTextHelper.
  private static final int TYPEFACE_SANS = 1;
  private static final int TYPEFACE_SERIF = 2;
  private static final int TYPEFACE_MONOSPACE = 3;

  public final float textSize;
  @Nullable
  public final ColorStateList textColor;
  @Nullable public final ColorStateList textColorHint;
  @Nullable public final ColorStateList textColorLink;
  public final int textStyle;
  public final int typeface;
  @Nullable public final String fontFamily;
  public final boolean textAllCaps;
  @Nullable public final ColorStateList shadowColor;
  public final float shadowDx;
  public final float shadowDy;
  public final float shadowRadius;

  @FontRes
  private final int fontFamilyResourceId;

  private boolean fontResolved = false;
  private Typeface font;

  /** Parses the given NRTextAppearance style resource. */
  public NRTextAppearance(Context context, @StyleRes int id) {
    TypedArray a = context.obtainStyledAttributes(id, R.styleable.TextAppearance);

    textSize = a.getDimension(R.styleable.TextAppearance_android_textSize, 0f);
    textColor =
        NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColor);
    textColorHint =
        NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorHint);
    textColorLink =
        NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorLink);
    textStyle = a.getInt(R.styleable.TextAppearance_android_textStyle, Typeface.NORMAL);
    typeface = a.getInt(R.styleable.TextAppearance_android_typeface, TYPEFACE_SANS);
    int fontFamilyIndex =
        NRMaterialResources.getIndexWithValue(
            a,
            R.styleable.TextAppearance_fontFamily,
            R.styleable.TextAppearance_android_fontFamily);
    fontFamilyResourceId = a.getResourceId(fontFamilyIndex, 0);
    fontFamily = a.getString(fontFamilyIndex);
    textAllCaps = a.getBoolean(R.styleable.TextAppearance_textAllCaps, false);
    shadowColor =
        NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_shadowColor);
    shadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0);
    shadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0);
    shadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0);

    a.recycle();
  }

  /**
   * Synchronously resolves the font Typeface using the fontFamily, style, and typeface.
   *
   * @see com.todou.nestrefresh.material.NRCollapsingTextHelper
   */
  @VisibleForTesting
  @NonNull
  public Typeface getFont(Context context) {
    if (fontResolved) {
      return font;
    }

    // Try resolving fontFamily as a font resource.
    if (!context.isRestricted()) {
      try {
        font = ResourcesCompat.getFont(context, fontFamilyResourceId);
        if (font != null) {
          font = Typeface.create(font, textStyle);
        }
      } catch (UnsupportedOperationException | Resources.NotFoundException e) {
        // Expected if it is not a font resource.
      } catch (Exception e) {
        Log.d(TAG, "Error loading font " + fontFamily, e);
      }
    }

    // If not resolved create fallback and resolve.
    createFallbackFont();
    fontResolved = true;

    return font;
  }

  /**
   * Resolves the requested font using the fontFamily, style, and typeface. Immediately (and
   * synchronously) calls {@link TextAppearanceFontCallback#onFontRetrieved(Typeface, boolean)} with
   * the requested font, if it has been resolved already, or {@link
   * TextAppearanceFontCallback#onFontRetrievalFailed(int)} if requested fontFamily is invalid.
   * Otherwise callback is invoked asynchronously when the font is loaded (or async loading fails).
   * While font is being fetched asynchronously, {@link #getFallbackFont()} can be used as a
   * temporary font.
   *
   * @param context the {@link Context}.
   * @param callback callback to notify when font is loaded.
   *
   */
  public void getFontAsync(Context context, @NonNull final TextAppearanceFontCallback callback) {
    if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
      getFont(context);
    } else {
      // No-op if font already resolved.
      createFallbackFont();
    }

    if (fontFamilyResourceId == 0) {
      // Only fontFamily id requires async fetch, if undefined the fallback font is the actual font.
      fontResolved = true;
    }

    if (fontResolved) {
      callback.onFontRetrieved(font, true);
      return;
    }

    // Try to resolve fontFamily asynchronously. If failed fallback font is used instead.
    try {
      ResourcesCompat.getFont(
          context,
          fontFamilyResourceId,
          new ResourcesCompat.FontCallback() {
            @Override
            public void onFontRetrieved(@NonNull Typeface typeface) {
              font = Typeface.create(typeface, textStyle);
              fontResolved = true;
              callback.onFontRetrieved(font, false);
            }

            @Override
            public void onFontRetrievalFailed(int reason) {
              fontResolved = true;
              callback.onFontRetrievalFailed(reason);
            }
          },
          /* handler */ null);
    } catch (Resources.NotFoundException e) {
      // Expected if it is not a font resource.
      fontResolved = true;
      callback.onFontRetrievalFailed(FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_NOT_FOUND);
    } catch (Exception e) {
      Log.d(TAG, "Error loading font " + fontFamily, e);
      fontResolved = true;
      callback.onFontRetrievalFailed(FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR);
    }
  }

  /**
   * Asynchronously resolves the requested font Typeface using the fontFamily, style, and typeface,
   * and automatically updates given {@code textPaint} using {@link #updateTextPaintMeasureState} on
   * successful load.
   *
   * @param context The {@link Context}.
   * @param textPaint {@link TextPaint} to be updated.
   * @param callback Callback to notify when font is available.
   * @see #getFontAsync(Context, TextAppearanceFontCallback)
   */
  public void getFontAsync(
      Context context,
      final TextPaint textPaint,
      @NonNull final TextAppearanceFontCallback callback) {
    // Updates text paint using fallback font while waiting for font to be requested.
    updateTextPaintMeasureState(textPaint, getFallbackFont());

    getFontAsync(
        context,
        new TextAppearanceFontCallback() {
          @Override
          public void onFontRetrieved(
              @NonNull Typeface typeface, boolean fontResolvedSynchronously) {
            updateTextPaintMeasureState(textPaint, typeface);
            callback.onFontRetrieved(typeface, fontResolvedSynchronously);
          }

          @Override
          public void onFontRetrievalFailed(int i) {
            callback.onFontRetrievalFailed(i);
          }
        });
  }

  /**
   * Returns a fallback {@link Typeface} that is retrieved synchronously, in case the actual font is
   * not yet resolved or pending async fetch or an actual {@link Typeface} if resolved already.
   *
   * <p>Fallback font is a font that can be resolved using typeface attributes not requiring any
   * async operations, i.e. android:typeface, android:textStyle and android:fontFamily defined as
   * string rather than resource id.
   */
  public Typeface getFallbackFont() {
    createFallbackFont();
    return font;
  }

  private void createFallbackFont() {
    // Try resolving fontFamily as a string name if specified.
    if (font == null && fontFamily != null) {
      font = Typeface.create(fontFamily, textStyle);
    }

    // Try resolving typeface if specified otherwise fallback to Typeface.DEFAULT.
    if (font == null) {
      switch (typeface) {
        case TYPEFACE_SANS:
          font = Typeface.SANS_SERIF;
          break;
        case TYPEFACE_SERIF:
          font = Typeface.SERIF;
          break;
        case TYPEFACE_MONOSPACE:
          font = Typeface.MONOSPACE;
          break;
        default:
          font = Typeface.DEFAULT;
          break;
      }
      font = Typeface.create(font, textStyle);
    }
  }

  // TODO: Move the TextPaint utilities below to a separate class.

  /**
   * Applies the attributes that affect drawing from NRTextAppearance to the given TextPaint. Note
   * that not all attributes can be applied to the TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateDrawState(TextPaint)
   */
  public void updateDrawState(
      Context context, TextPaint textPaint, @NonNull TextAppearanceFontCallback callback) {
    updateMeasureState(context, textPaint, callback);

    textPaint.setColor(
        textColor != null
            ? textColor.getColorForState(textPaint.drawableState, textColor.getDefaultColor())
            : Color.BLACK);
    textPaint.setShadowLayer(
        shadowRadius,
        shadowDx,
        shadowDy,
        shadowColor != null
            ? shadowColor.getColorForState(textPaint.drawableState, shadowColor.getDefaultColor())
            : Color.TRANSPARENT);
  }

  /**
   * Applies the attributes that affect measurement from NRTextAppearance to the given TextPaint. Note
   * that not all attributes can be applied to the TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateMeasureState(TextPaint)
   */
  public void updateMeasureState(
      Context context, TextPaint textPaint, @NonNull TextAppearanceFontCallback callback) {
    if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
      updateTextPaintMeasureState(textPaint, getFont(context));
    } else {
      getFontAsync(context, textPaint, callback);
    }
  }

  /**
   * Applies the attributes that affect measurement from Typeface to the given TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateMeasureState(TextPaint)
   */
  public void updateTextPaintMeasureState(
      @NonNull TextPaint textPaint, @NonNull Typeface typeface) {
    textPaint.setTypeface(typeface);

    int fake = textStyle & ~typeface.getStyle();
    textPaint.setFakeBoldText((fake & Typeface.BOLD) != 0);
    textPaint.setTextSkewX((fake & Typeface.ITALIC) != 0 ? -0.25f : 0f);

    textPaint.setTextSize(textSize);
  }
}