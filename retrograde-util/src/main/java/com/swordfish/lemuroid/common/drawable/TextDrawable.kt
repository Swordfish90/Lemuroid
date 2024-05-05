package com.swordfish.lemuroid.common.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint

class TextDrawable(private val text: String, private val color: Int) : Drawable() {
    companion object {
        private const val DEFAULT_COLOR = Color.WHITE
    }

    private val mTextBounds = Rect()
    private val mPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    init {
        mPaint.color = DEFAULT_COLOR
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.typeface = Typeface.MONOSPACE
        mPaint.getTextBounds(text, 0, text.length, mTextBounds)
    }

    override fun draw(canvas: Canvas) {
        mPaint.color = color
        canvas.drawRect(bounds, mPaint)

        mPaint.color = DEFAULT_COLOR
        mPaint.textSize = bounds.height() * 0.3f
        val xPos = bounds.width().toFloat() / 2
        val yPos = (bounds.height().toFloat() / 2 - (mPaint.descent() + mPaint.ascent()) / 2)
        canvas.drawText(text, xPos, yPos, mPaint)
    }

    override fun getOpacity(): Int = mPaint.alpha

    override fun getIntrinsicWidth(): Int = -1

    override fun getIntrinsicHeight(): Int = -1

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(p0: ColorFilter?) {
        mPaint.colorFilter = p0
        invalidateSelf()
    }
}
