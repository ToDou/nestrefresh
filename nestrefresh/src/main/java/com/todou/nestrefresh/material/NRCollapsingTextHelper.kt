package com.todou.nestrefresh.material

import android.animation.TimeInterpolator
import android.content.res.ColorStateList
import android.graphics.*
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.math.MathUtils
import android.support.v4.text.TextDirectionHeuristicsCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.todou.nestrefresh.material.animation.NRAnimationUtils
import com.todou.nestrefresh.material.resources.NRCancelableFontCallback
import com.todou.nestrefresh.material.resources.NRTextAppearance

class NRCollapsingTextHelper(private val view: View) {

    private var drawTitle: Boolean = false
    /**
     * Set the value indicating the current scroll value. This decides how much of the background will
     * be displayed, as well as the title metrics/positioning.
     *
     *
     * A value of `0.0` indicates that the layout is fully expanded. A value of `1.0`
     * indicates that the layout is fully collapsed.
     */
    var expansionFraction: Float = 0.toFloat()
        set(fraction) {
            var fraction = fraction
            fraction = MathUtils.clamp(fraction, 0f, 1f)

            if (fraction != expansionFraction) {
                field = fraction
                calculateCurrentOffsets()
            }
        }

    private val expandedBounds: Rect
    private val collapsedBounds: Rect
    private val currentBounds: RectF
    var expandedTextGravity = Gravity.CENTER_VERTICAL
        set(gravity) {
            if (this.expandedTextGravity != gravity) {
                field = gravity
                recalculate()
            }
        }
    var collapsedTextGravity = Gravity.CENTER_VERTICAL
        set(gravity) {
            if (this.collapsedTextGravity != gravity) {
                field = gravity
                recalculate()
            }
        }
    private var expandedTextSize = 15f
    private var collapsedTextSize = 15f
    private var expandedTextColor: ColorStateList? = null
    private var collapsedTextColor: ColorStateList? = null

    private var expandedDrawY: Float = 0.toFloat()
    private var collapsedDrawY: Float = 0.toFloat()
    private var expandedDrawX: Float = 0.toFloat()
    private var collapsedDrawX: Float = 0.toFloat()
    private var currentDrawX: Float = 0.toFloat()
    private var currentDrawY: Float = 0.toFloat()
    private var collapsedTypeface: Typeface? = null
    private var expandedTypeface: Typeface? = null
    private var currentTypeface: Typeface? = null
    private var expandedFontCallback: NRCancelableFontCallback? = null
    private var collapsedFontCallback: NRCancelableFontCallback? = null

    /**
     * Set the title to display
     *
     * @param text
     */
    var text: CharSequence? = null
        set(text) {
            if (text == null || !TextUtils.equals(this.text, text)) {
                field = text
                textToDraw = null
                clearTexture()
                recalculate()
            }
        }
    private var textToDraw: CharSequence? = null
    private var isRtl: Boolean = false

    private var useTexture: Boolean = false
    private var expandedTitleTexture: Bitmap? = null
    private var texturePaint: Paint? = null
    private var textureAscent: Float = 0.toFloat()
    private var textureDescent: Float = 0.toFloat()

    private var scale: Float = 0.toFloat()
    private var currentTextSize: Float = 0.toFloat()

    private var state: IntArray? = null

    private var boundsChanged: Boolean = false

    private val textPaint: TextPaint
    private val tmpPaint: TextPaint

    private var positionInterpolator: TimeInterpolator? = null
    private var textSizeInterpolator: TimeInterpolator? = null

    private var collapsedShadowRadius: Float = 0.toFloat()
    private var collapsedShadowDx: Float = 0.toFloat()
    private var collapsedShadowDy: Float = 0.toFloat()
    private var collapsedShadowColor: ColorStateList? = null

    private var expandedShadowRadius: Float = 0.toFloat()
    private var expandedShadowDx: Float = 0.toFloat()
    private var expandedShadowDy: Float = 0.toFloat()
    private var expandedShadowColor: ColorStateList? = null

    // Return expanded height measured from the baseline.
    val expandedTextHeight: Float
        get() {
            getTextPaintExpanded(tmpPaint)
            return -tmpPaint.ascent()
        }

    // Return collapsed height measured from the baseline.
    val collapsedTextHeight: Float
        get() {
            getTextPaintCollapsed(tmpPaint)
            return -tmpPaint.ascent()
        }

    val isStateful: Boolean
        get() = collapsedTextColor != null && collapsedTextColor!!.isStateful || expandedTextColor != null && expandedTextColor!!.isStateful

    private val currentExpandedTextColor: Int
        @ColorInt
        get() = getCurrentColor(expandedTextColor)

    val currentCollapsedTextColor: Int
        @ColorInt
        get() = getCurrentColor(collapsedTextColor)

    init {

        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        tmpPaint = TextPaint(textPaint)

        collapsedBounds = Rect()
        expandedBounds = Rect()
        currentBounds = RectF()
    }

    fun setTextSizeInterpolator(interpolator: TimeInterpolator) {
        textSizeInterpolator = interpolator
        recalculate()
    }

    fun setPositionInterpolator(interpolator: TimeInterpolator) {
        positionInterpolator = interpolator
        recalculate()
    }

    fun setExpandedTextSize(textSize: Float) {
        if (expandedTextSize != textSize) {
            expandedTextSize = textSize
            recalculate()
        }
    }

    fun setCollapsedTextSize(textSize: Float) {
        if (collapsedTextSize != textSize) {
            collapsedTextSize = textSize
            recalculate()
        }
    }

    fun setCollapsedTextColor(textColor: ColorStateList) {
        if (collapsedTextColor !== textColor) {
            collapsedTextColor = textColor
            recalculate()
        }
    }

    fun setExpandedTextColor(textColor: ColorStateList) {
        if (expandedTextColor !== textColor) {
            expandedTextColor = textColor
            recalculate()
        }
    }

    fun setExpandedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(expandedBounds, left, top, right, bottom)) {
            expandedBounds.set(left, top, right, bottom)
            boundsChanged = true
            onBoundsChanged()
        }
    }

    fun setExpandedBounds(bounds: Rect) {
        setExpandedBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    fun setCollapsedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
            collapsedBounds.set(left, top, right, bottom)
            boundsChanged = true
            onBoundsChanged()
        }
    }

    fun setCollapsedBounds(bounds: Rect) {
        setCollapsedBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    fun getCollapsedTextActualBounds(bounds: RectF) {
        val isRtl = calculateIsRtl(this.text)

        bounds.left = if (!isRtl) collapsedBounds.left.toFloat() else collapsedBounds.right - calculateCollapsedTextWidth()
        bounds.top = collapsedBounds.top.toFloat()
        bounds.right = if (!isRtl) bounds.left + calculateCollapsedTextWidth() else collapsedBounds.right.toFloat()
        bounds.bottom = collapsedBounds.top + collapsedTextHeight
    }

    fun calculateCollapsedTextWidth(): Float {
        if (this.text == null) {
            return 0f
        }
        getTextPaintCollapsed(tmpPaint)
        return tmpPaint.measureText(this.text, 0, this.text!!.length)
    }

    private fun getTextPaintExpanded(textPaint: TextPaint) {
        textPaint.textSize = expandedTextSize
        textPaint.typeface = expandedTypeface
    }

    private fun getTextPaintCollapsed(textPaint: TextPaint) {
        textPaint.textSize = collapsedTextSize
        textPaint.typeface = collapsedTypeface
    }

    internal fun onBoundsChanged() {
        drawTitle = (collapsedBounds.width() > 0
                && collapsedBounds.height() > 0
                && expandedBounds.width() > 0
                && expandedBounds.height() > 0)
    }

    fun setCollapsedTextAppearance(resId: Int) {
        val textAppearance = NRTextAppearance(view.context, resId)

        if (textAppearance.textColor != null) {
            collapsedTextColor = textAppearance.textColor
        }
        if (textAppearance.textSize != 0f) {
            collapsedTextSize = textAppearance.textSize
        }
        if (textAppearance.shadowColor != null) {
            collapsedShadowColor = textAppearance.shadowColor
        }
        collapsedShadowDx = textAppearance.shadowDx
        collapsedShadowDy = textAppearance.shadowDy
        collapsedShadowRadius = textAppearance.shadowRadius

        // Cancel pending async fetch, if any, and replace with a new one.
        if (collapsedFontCallback != null) {
            collapsedFontCallback!!.cancel()
        }
        collapsedFontCallback = NRCancelableFontCallback(
            object : NRCancelableFontCallback.ApplyFont {
                override fun apply(font: Typeface) {
                    setCollapsedTypeface(font)
                }
            },
            textAppearance.fallbackFont!!
        )
        textAppearance.getFontAsync(view.context, collapsedFontCallback!!)

        recalculate()
    }

    fun setExpandedTextAppearance(resId: Int) {
        val textAppearance = NRTextAppearance(view.context, resId)
        if (textAppearance.textColor != null) {
            expandedTextColor = textAppearance.textColor
        }
        if (textAppearance.textSize != 0f) {
            expandedTextSize = textAppearance.textSize
        }
        if (textAppearance.shadowColor != null) {
            expandedShadowColor = textAppearance.shadowColor
        }
        expandedShadowDx = textAppearance.shadowDx
        expandedShadowDy = textAppearance.shadowDy
        expandedShadowRadius = textAppearance.shadowRadius

        // Cancel pending async fetch, if any, and replace with a new one.
        if (expandedFontCallback != null) {
            expandedFontCallback!!.cancel()
        }
        expandedFontCallback = NRCancelableFontCallback(
            object : NRCancelableFontCallback.ApplyFont {
                override fun apply(font: Typeface) {
                    setExpandedTypeface(font)
                }
            },
            textAppearance.fallbackFont!!
        )
        textAppearance.getFontAsync(view.context, expandedFontCallback!!)

        recalculate()
    }

    fun setCollapsedTypeface(typeface: Typeface) {
        if (setCollapsedTypefaceInternal(typeface)) {
            recalculate()
        }
    }

    fun setExpandedTypeface(typeface: Typeface) {
        if (setExpandedTypefaceInternal(typeface)) {
            recalculate()
        }
    }

    fun setTypefaces(typeface: Typeface) {
        val collapsedFontChanged = setCollapsedTypefaceInternal(typeface)
        val expandedFontChanged = setExpandedTypefaceInternal(typeface)
        if (collapsedFontChanged || expandedFontChanged) {
            recalculate()
        }
    }

    private// Matches the Typeface comparison in TextView
    fun setCollapsedTypefaceInternal(typeface: Typeface): Boolean {
        // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
        // already updated one when async op comes back after a while.
        if (collapsedFontCallback != null) {
            collapsedFontCallback!!.cancel()
        }
        if (collapsedTypeface !== typeface) {
            collapsedTypeface = typeface
            return true
        }
        return false
    }

    private// Matches the Typeface comparison in TextView
    fun setExpandedTypefaceInternal(typeface: Typeface): Boolean {
        // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
        // already updated one when async op comes back after a while.
        if (expandedFontCallback != null) {
            expandedFontCallback!!.cancel()
        }
        if (expandedTypeface !== typeface) {
            expandedTypeface = typeface
            return true
        }
        return false
    }

    fun getCollapsedTypeface(): Typeface {
        return collapsedTypeface ?: Typeface.DEFAULT
    }

    fun getExpandedTypeface(): Typeface {
        return expandedTypeface ?: Typeface.DEFAULT
    }

    fun setState(state: IntArray): Boolean {
        this.state = state

        if (isStateful) {
            recalculate()
            return true
        }

        return false
    }

    fun getCollapsedTextSize(): Float {
        return collapsedTextSize
    }

    fun getExpandedTextSize(): Float {
        return expandedTextSize
    }

    private fun calculateCurrentOffsets() {
        calculateOffsets(expansionFraction)
    }

    private fun calculateOffsets(fraction: Float) {
        interpolateBounds(fraction)
        currentDrawX = lerp(expandedDrawX, collapsedDrawX, fraction, positionInterpolator)
        currentDrawY = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator)

        setInterpolatedTextSize(
            lerp(expandedTextSize, collapsedTextSize, fraction, textSizeInterpolator)
        )

        if (collapsedTextColor !== expandedTextColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            textPaint.color = blendColors(currentExpandedTextColor, currentCollapsedTextColor, fraction)
        } else {
            textPaint.color = currentCollapsedTextColor
        }

        textPaint.setShadowLayer(
            lerp(expandedShadowRadius, collapsedShadowRadius, fraction, null),
            lerp(expandedShadowDx, collapsedShadowDx, fraction, null),
            lerp(expandedShadowDy, collapsedShadowDy, fraction, null),
            blendColors(
                getCurrentColor(expandedShadowColor), getCurrentColor(collapsedShadowColor), fraction
            )
        )

        ViewCompat.postInvalidateOnAnimation(view)
    }

    @ColorInt
    private fun getCurrentColor(colorStateList: ColorStateList?): Int {
        if (colorStateList == null) {
            return 0
        }
        return if (state != null) {
            colorStateList.getColorForState(state, 0)
        } else colorStateList.defaultColor
    }

    private fun calculateBaseOffsets() {
        val currentTextSize = this.currentTextSize

        // We then calculate the collapsed text size, using the same logic
        calculateUsingTextSize(collapsedTextSize)
        var width = if (textToDraw != null) textPaint.measureText(textToDraw, 0, textToDraw!!.length) else 0f
        val collapsedAbsGravity = GravityCompat.getAbsoluteGravity(
            this.collapsedTextGravity,
            if (isRtl) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR
        )
        when (collapsedAbsGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> collapsedDrawY = collapsedBounds.bottom.toFloat()
            Gravity.TOP -> collapsedDrawY = collapsedBounds.top - textPaint.ascent()
            Gravity.CENTER_VERTICAL -> {
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textOffset = textHeight / 2 - textPaint.descent()
                collapsedDrawY = collapsedBounds.centerY() + textOffset
            }
            else -> {
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textOffset = textHeight / 2 - textPaint.descent()
                collapsedDrawY = collapsedBounds.centerY() + textOffset
            }
        }
        when (collapsedAbsGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> collapsedDrawX = collapsedBounds.centerX() - width / 2
            Gravity.RIGHT -> collapsedDrawX = collapsedBounds.right.minus(width)
            Gravity.LEFT -> collapsedDrawX = collapsedBounds.left.toFloat()
            else -> collapsedDrawX = collapsedBounds.left.toFloat()
        }

        calculateUsingTextSize(expandedTextSize)
        width = if (textToDraw != null) textPaint.measureText(textToDraw, 0, textToDraw!!.length) else 0f
        val expandedAbsGravity = GravityCompat.getAbsoluteGravity(
            this.expandedTextGravity,
            if (isRtl) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR
        )
        when (expandedAbsGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> expandedDrawY = expandedBounds.bottom.toFloat()
            Gravity.TOP -> expandedDrawY = expandedBounds.top - textPaint.ascent()
            Gravity.CENTER_VERTICAL -> {
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textOffset = textHeight / 2 - textPaint.descent()
                expandedDrawY = expandedBounds.centerY() + textOffset
            }
            else -> {
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textOffset = textHeight / 2 - textPaint.descent()
                expandedDrawY = expandedBounds.centerY() + textOffset
            }
        }
        when (expandedAbsGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> expandedDrawX = expandedBounds.centerX() - width / 2
            Gravity.RIGHT -> expandedDrawX = expandedBounds.right - width
            Gravity.LEFT -> expandedDrawX = expandedBounds.left.toFloat()
            else -> expandedDrawX = expandedBounds.left.toFloat()
        }

        // The bounds have changed so we need to clear the texture
        clearTexture()
        // Now reset the text size back to the original
        setInterpolatedTextSize(currentTextSize)
    }

    private fun interpolateBounds(fraction: Float) {
        currentBounds.left =
            lerp(expandedBounds.left.toFloat(), collapsedBounds.left.toFloat(), fraction, positionInterpolator)
        currentBounds.top = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator)
        currentBounds.right =
            lerp(expandedBounds.right.toFloat(), collapsedBounds.right.toFloat(), fraction, positionInterpolator)
        currentBounds.bottom =
            lerp(expandedBounds.bottom.toFloat(), collapsedBounds.bottom.toFloat(), fraction, positionInterpolator)
    }

    fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        if (textToDraw != null && drawTitle) {
            val x = currentDrawX
            var y = currentDrawY

            val drawTexture = useTexture && expandedTitleTexture != null

            val ascent: Float
            val descent: Float
            if (drawTexture) {
                ascent = textureAscent * scale
                descent = textureDescent * scale
            } else {
                ascent = textPaint.ascent() * scale
                descent = textPaint.descent() * scale
            }

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a magenta rect in the text bounds
                canvas.drawRect(
                    currentBounds.left, y + ascent, currentBounds.right, y + descent, DEBUG_DRAW_PAINT!!
                )
            }

            if (drawTexture) {
                y += ascent
            }

            if (scale != 1f) {
                canvas.scale(scale, scale, x, y)
            }

            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                canvas.drawBitmap(expandedTitleTexture!!, x, y, texturePaint)
            } else {
                canvas.drawText(textToDraw!!, 0, textToDraw!!.length, x, y, textPaint)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    private fun calculateIsRtl(text: CharSequence?): Boolean {
        val defaultIsRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
        return (if (defaultIsRtl)
            TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
        else
            TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR)
            .isRtl(text, 0, text!!.length)
    }

    private fun setInterpolatedTextSize(textSize: Float) {
        calculateUsingTextSize(textSize)

        // Use our texture if the scale isn't 1.0
        useTexture = USE_SCALING_TEXTURE && scale != 1f

        if (useTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture()
        }

        ViewCompat.postInvalidateOnAnimation(view)
    }

    private// Matches the Typeface comparison in TextView
    fun calculateUsingTextSize(textSize: Float) {
        if (this.text == null) {
            return
        }

        val collapsedWidth = collapsedBounds.width().toFloat()
        val expandedWidth = expandedBounds.width().toFloat()

        val availableWidth: Float
        val newTextSize: Float
        var updateDrawText = false

        if (isClose(textSize, collapsedTextSize)) {
            newTextSize = collapsedTextSize
            scale = 1f
            if (currentTypeface !== collapsedTypeface) {
                currentTypeface = collapsedTypeface
                updateDrawText = true
            }
            availableWidth = collapsedWidth
        } else {
            newTextSize = expandedTextSize
            if (currentTypeface !== expandedTypeface) {
                currentTypeface = expandedTypeface
                updateDrawText = true
            }
            if (isClose(textSize, expandedTextSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                scale = 1f
            } else {
                // Else, we'll scale down from the expanded text size
                scale = textSize / expandedTextSize
            }

            val textSizeRatio = collapsedTextSize / expandedTextSize
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed text size
            val scaledDownWidth = expandedWidth * textSizeRatio

            if (scaledDownWidth > collapsedWidth) {
                // If the scaled down size is larger than the actual collapsed width, we need to
                // cap the available width so that when the expanded text scales down, it matches
                // the collapsed width
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth)
            } else {
                // Otherwise we'll just use the expanded width
                availableWidth = expandedWidth
            }
        }

        if (availableWidth > 0) {
            updateDrawText = currentTextSize != newTextSize || boundsChanged || updateDrawText
            currentTextSize = newTextSize
            boundsChanged = false
        }

        if (textToDraw == null || updateDrawText) {
            textPaint.textSize = currentTextSize
            textPaint.typeface = currentTypeface
            // Use linear text scaling if we're scaling the canvas
            textPaint.isLinearText = scale != 1f

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            val title = TextUtils.ellipsize(this.text, textPaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(title, textToDraw)) {
                textToDraw = title
                isRtl = calculateIsRtl(textToDraw)
            }
        }
    }

    private fun ensureExpandedTexture() {
        if (expandedTitleTexture != null || expandedBounds.isEmpty || TextUtils.isEmpty(textToDraw)) {
            return
        }

        calculateOffsets(0f)
        textureAscent = textPaint.ascent()
        textureDescent = textPaint.descent()

        val w = Math.round(textPaint.measureText(textToDraw, 0, textToDraw!!.length))
        val h = Math.round(textureDescent - textureAscent)

        if (w <= 0 || h <= 0) {
            return  // If the width or height are 0, return
        }

        expandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val c = Canvas(expandedTitleTexture!!)
        c.drawText(textToDraw!!, 0, textToDraw!!.length, 0f, h - textPaint.descent(), textPaint)

        if (texturePaint == null) {
            // Make sure we have a paint
            texturePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        }
    }

    fun recalculate() {
        if (view.height > 0 && view.width > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets()
            calculateCurrentOffsets()
        }
    }

    private fun clearTexture() {
        if (expandedTitleTexture != null) {
            expandedTitleTexture!!.recycle()
            expandedTitleTexture = null
        }
    }

    fun getExpandedTextColor(): ColorStateList? {
        return expandedTextColor
    }

    fun getCollapsedTextColor(): ColorStateList? {
        return collapsedTextColor
    }

    companion object {

        // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
        // by using our own texture
        private val USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18

        private val DEBUG_DRAW = false
        private val DEBUG_DRAW_PAINT: Paint?

        init {
            DEBUG_DRAW_PAINT = if (DEBUG_DRAW) Paint() else null
            if (DEBUG_DRAW_PAINT != null) {
                DEBUG_DRAW_PAINT.isAntiAlias = true
                DEBUG_DRAW_PAINT.color = Color.MAGENTA
            }
        }

        /**
         * Returns true if `value` is 'close' to it's closest decimal value. Close is currently
         * defined as it's difference being < 0.001.
         */
        private fun isClose(value: Float, targetValue: Float): Boolean {
            return Math.abs(value - targetValue) < 0.001f
        }

        /**
         * Blend `color1` and `color2` using the given ratio.
         *
         * @param ratio of which to blend. 0.0 will return `color1`, 0.5 will give an even blend,
         * 1.0 will return `color2`.
         */
        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val a = Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio
            val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
            val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
            val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
            return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
        }

        private fun lerp(
            startValue: Float, endValue: Float, fraction: Float, interpolator: TimeInterpolator?
        ): Float {
            var fraction = fraction
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction)
            }
            return NRAnimationUtils.lerp(startValue, endValue, fraction)
        }

        private fun rectEquals(r: Rect, left: Int, top: Int, right: Int, bottom: Int): Boolean {
            return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom)
        }
    }
}