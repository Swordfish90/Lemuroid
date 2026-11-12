package com.swordfish.lemuroid.common.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint

abstract class BaseCoverPlaceholderDrawable(
    private val backgroundColor: Int,
) : Drawable() {
    protected val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TEXT_COLOR
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private var drawableAlpha = 255

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        backgroundShader = LinearGradient(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            backgroundColor,
            backgroundColor.darken(),
            Shader.TileMode.CLAMP,
        )
    }

    private var backgroundShader: LinearGradient? = null

    final override fun draw(canvas: Canvas) {
        drawBackground(canvas)
        drawSpine(canvas)
        drawContent(canvas)
    }

    protected abstract fun drawContent(canvas: Canvas)

    protected fun useTextPaint(alpha: Int = 255) {
        paint.shader = null
        paint.color = TEXT_COLOR
        paint.alpha = scaledAlpha(alpha)
    }

    protected fun fitSingleLine(text: String, textSize: Float, maxWidth: Float) {
        paint.textSize = textSize
        val measuredWidth = paint.measureText(text)
        if (measuredWidth > maxWidth) {
            paint.textSize *= maxWidth / measuredWidth
        }
    }

    protected fun centeredBaseline(centerY: Float) = centerY - (paint.ascent() + paint.descent()) / 2f

    private fun drawBackground(canvas: Canvas) {
        paint.shader = backgroundShader
        paint.alpha = drawableAlpha
        canvas.drawRect(bounds, paint)
        paint.shader = null
    }

    private fun drawSpine(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.alpha = scaledAlpha(SPINE_ALPHA)
        canvas.drawRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left + bounds.width() * SPINE_WIDTH_FRACTION,
            bounds.bottom.toFloat(),
            paint,
        )
    }

    private fun scaledAlpha(alpha: Int) = alpha * drawableAlpha / 255

    @Suppress("OVERRIDE_DEPRECATION")
    final override fun getOpacity() = PixelFormat.TRANSLUCENT

    final override fun getIntrinsicWidth() = -1

    final override fun getIntrinsicHeight() = -1

    final override fun setAlpha(alpha: Int) {
        drawableAlpha = alpha
        invalidateSelf()
    }

    final override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    protected companion object {
        const val TEXT_COLOR = Color.WHITE
        const val SPINE_WIDTH_FRACTION = 0.18f
        const val SPINE_CENTER_X_FRACTION = SPINE_WIDTH_FRACTION / 2f

        private const val SPINE_ALPHA = 52
    }
}

private fun Int.darken() = Color.rgb(Color.red(this) * 2 / 3, Color.green(this) * 2 / 3, Color.blue(this) * 2 / 3)
