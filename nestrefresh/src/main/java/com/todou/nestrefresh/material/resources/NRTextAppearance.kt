package com.todou.nestrefresh.material.resources

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.provider.FontsContractCompat
import android.text.TextPaint
import android.util.Log
import com.google.android.material.resources.TextAppearanceConfig
import com.todou.nestrefresh.R
import com.todou.nestrefresh.material.TextAppearanceFontCallback

class NRTextAppearance
/** Parses the given NRTextAppearance style resource.  */
    (context: Context, @StyleRes id: Int) {

    val textSize: Float
    val textColor: ColorStateList?
    val textColorHint: ColorStateList?
    val textColorLink: ColorStateList?
    val textStyle: Int
    val typeface: Int
    val fontFamily: String?
    val textAllCaps: Boolean
    val shadowColor: ColorStateList?
    val shadowDx: Float
    val shadowDy: Float
    val shadowRadius: Float

    @FontRes
    private val fontFamilyResourceId: Int

    private var fontResolved = false
    private lateinit var font: Typeface

    /**
     * Returns a fallback [Typeface] that is retrieved synchronously, in case the actual font is
     * not yet resolved or pending async fetch or an actual [Typeface] if resolved already.
     *
     *
     * Fallback font is a font that can be resolved using typeface attributes not requiring any
     * async operations, i.e. android:typeface, android:textStyle and android:fontFamily defined as
     * string rather than resource id.
     */
    val fallbackFont: Typeface?
        get() {
            createFallbackFont()
            return font
        }

    init {
        val a = context.obtainStyledAttributes(id, R.styleable.TextAppearance)

        textSize = a.getDimension(R.styleable.TextAppearance_android_textSize, 0f)
        textColor = NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColor
        )
        textColorHint = NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorHint
        )
        textColorLink = NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorLink
        )
        textStyle = a.getInt(R.styleable.TextAppearance_android_textStyle, Typeface.NORMAL)
        typeface = a.getInt(R.styleable.TextAppearance_android_typeface, TYPEFACE_SANS)
        val fontFamilyIndex = NRMaterialResources.getIndexWithValue(
            a,
            R.styleable.TextAppearance_fontFamily,
            R.styleable.TextAppearance_android_fontFamily
        )
        fontFamilyResourceId = a.getResourceId(fontFamilyIndex, 0)
        fontFamily = a.getString(fontFamilyIndex)
        textAllCaps = a.getBoolean(R.styleable.TextAppearance_textAllCaps, false)
        shadowColor = NRMaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_shadowColor
        )
        shadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0f)
        shadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0f)
        shadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0f)

        a.recycle()
    }

    /**
     * Synchronously resolves the font Typeface using the fontFamily, style, and typeface.
     *
     * @see com.todou.nestrefresh.material.NRCollapsingTextHelper
     */
    @VisibleForTesting
    fun getFont(context: Context): Typeface {
        if (fontResolved) {
            return font
        }

        // Try resolving fontFamily as a font resource.
        if (!context.isRestricted) {
            try {
                val typeface = ResourcesCompat.getFont(context, fontFamilyResourceId)
                if (typeface != null) {
                    font = Typeface.create(font, textStyle)
                }
            } catch (e: UnsupportedOperationException) {
                // Expected if it is not a font resource.
            } catch (e: Resources.NotFoundException) {
            } catch (e: Exception) {
                Log.d(TAG, "Error loading font " + fontFamily!!, e)
            }

        }

        // If not resolved create fallback and resolve.
        createFallbackFont()
        fontResolved = true

        return font
    }

    /**
     * Resolves the requested font using the fontFamily, style, and typeface. Immediately (and
     * synchronously) calls [TextAppearanceFontCallback.onFontRetrieved] with
     * the requested font, if it has been resolved already, or [ ][TextAppearanceFontCallback.onFontRetrievalFailed] if requested fontFamily is invalid.
     * Otherwise callback is invoked asynchronously when the font is loaded (or async loading fails).
     * While font is being fetched asynchronously, [.getFallbackFont] can be used as a
     * temporary font.
     *
     * @param context the [Context].
     * @param callback callback to notify when font is loaded.
     */
    fun getFontAsync(context: Context, callback: TextAppearanceFontCallback) {
        if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
            getFont(context)
        } else {
            // No-op if font already resolved.
            createFallbackFont()
        }

        if (fontFamilyResourceId == 0) {
            // Only fontFamily id requires async fetch, if undefined the fallback font is the actual font.
            fontResolved = true
        }

        if (fontResolved) {
            callback.onFontRetrieved(font, true)
            return
        }

        // Try to resolve fontFamily asynchronously. If failed fallback font is used instead.
        try {
            ResourcesCompat.getFont(
                context,
                fontFamilyResourceId,
                object : ResourcesCompat.FontCallback() {
                    override fun onFontRetrieved(typeface: Typeface) {
                        font = Typeface.create(typeface, textStyle)
                        fontResolved = true
                        callback.onFontRetrieved(font, false)
                    }

                    override fun onFontRetrievalFailed(reason: Int) {
                        fontResolved = true
                        callback.onFontRetrievalFailed(reason)
                    }
                }, null
            )/* handler */
        } catch (e: Resources.NotFoundException) {
            // Expected if it is not a font resource.
            fontResolved = true
            callback.onFontRetrievalFailed(FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_NOT_FOUND)
        } catch (e: Exception) {
            Log.d(TAG, "Error loading font " + fontFamily!!, e)
            fontResolved = true
            callback.onFontRetrievalFailed(FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR)
        }

    }

    /**
     * Asynchronously resolves the requested font Typeface using the fontFamily, style, and typeface,
     * and automatically updates given `textPaint` using [.updateTextPaintMeasureState] on
     * successful load.
     *
     * @param context The [Context].
     * @param textPaint [TextPaint] to be updated.
     * @param callback Callback to notify when font is available.
     * @see .getFontAsync
     */
    fun getFontAsync(
        context: Context,
        textPaint: TextPaint,
        callback: TextAppearanceFontCallback
    ) {
        // Updates text paint using fallback font while waiting for font to be requested.
        updateTextPaintMeasureState(textPaint, fallbackFont!!)

        getFontAsync(
            context,
            object : TextAppearanceFontCallback() {
                override fun onFontRetrieved(
                    typeface: Typeface, fontResolvedSynchronously: Boolean
                ) {
                    updateTextPaintMeasureState(textPaint, typeface)
                    callback.onFontRetrieved(typeface, fontResolvedSynchronously)
                }

                override fun onFontRetrievalFailed(i: Int) {
                    callback.onFontRetrievalFailed(i)
                }
            })
    }

    private fun createFallbackFont() {
        // Try resolving fontFamily as a string name if specified.
        if (!this::font.isInitialized && fontFamily != null) {
            font = Typeface.create(fontFamily, textStyle)
        }

        // Try resolving typeface if specified otherwise fallback to Typeface.DEFAULT.
        if (!this::font.isInitialized) {
            font = when (typeface) {
                TYPEFACE_SANS -> Typeface.SANS_SERIF
                TYPEFACE_SERIF -> Typeface.SERIF
                TYPEFACE_MONOSPACE -> Typeface.MONOSPACE
                else -> Typeface.DEFAULT
            }
            font = Typeface.create(font, textStyle)
        }
    }

    // TODO: Move the TextPaint utilities below to a separate class.

    /**
     * Applies the attributes that affect drawing from NRTextAppearance to the given TextPaint. Note
     * that not all attributes can be applied to the TextPaint.
     *
     * @see android.text.style.TextAppearanceSpan.updateDrawState
     */
    fun updateDrawState(
        context: Context, textPaint: TextPaint, callback: TextAppearanceFontCallback
    ) {
        updateMeasureState(context, textPaint, callback)

        textPaint.color = textColor?.getColorForState(textPaint.drawableState, textColor.defaultColor) ?: Color.BLACK
        textPaint.setShadowLayer(
            shadowRadius,
            shadowDx,
            shadowDy,
            shadowColor?.getColorForState(textPaint.drawableState, shadowColor.defaultColor) ?: Color.TRANSPARENT
        )
    }

    /**
     * Applies the attributes that affect measurement from NRTextAppearance to the given TextPaint. Note
     * that not all attributes can be applied to the TextPaint.
     *
     * @see android.text.style.TextAppearanceSpan.updateMeasureState
     */
    fun updateMeasureState(
        context: Context, textPaint: TextPaint, callback: TextAppearanceFontCallback
    ) {
        if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
            updateTextPaintMeasureState(textPaint, getFont(context))
        } else {
            getFontAsync(context, textPaint, callback)
        }
    }

    /**
     * Applies the attributes that affect measurement from Typeface to the given TextPaint.
     *
     * @see android.text.style.TextAppearanceSpan.updateMeasureState
     */
    fun updateTextPaintMeasureState(
        textPaint: TextPaint, typeface: Typeface
    ) {
        textPaint.typeface = typeface

        val fake = textStyle and typeface.style.inv()
        textPaint.isFakeBoldText = fake and Typeface.BOLD != 0
        textPaint.textSkewX = if (fake and Typeface.ITALIC != 0) -0.25f else 0f

        textPaint.textSize = textSize
    }

    companion object {

        private val TAG = "NRTextAppearance"

        // Enums from AppCompatTextHelper.
        private val TYPEFACE_SANS = 1
        private val TYPEFACE_SERIF = 2
        private val TYPEFACE_MONOSPACE = 3
    }
}