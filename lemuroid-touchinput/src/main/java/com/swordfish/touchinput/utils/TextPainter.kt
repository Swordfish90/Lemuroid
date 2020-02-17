package com.swordfish.touchinput.utils

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.swordfish.touchinput.controller.R

class TextPainter(resources: Resources) {
    private val textPaint = Paint().apply {
        this.color = resources.getColor(R.color.touch_control_text_color)
        this.typeface = Typeface.DEFAULT_BOLD
        this.style = Paint.Style.FILL
    }

    fun paintText(left: Float, top: Float, width: Float, height: Float, text: String, canvas: Canvas) {
        textPaint.textSize = minOf(height, width) / 3f
        val textWidth = textPaint.measureText(text)

        val xPos = left - textWidth / 2f + width / 2f
        val yPos = top + height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        canvas.drawText(text, xPos, yPos, textPaint)
    }
}
