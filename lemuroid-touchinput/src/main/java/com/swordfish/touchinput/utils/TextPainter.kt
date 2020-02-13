package com.swordfish.touchinput.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object TextPainter {
    private val textPaint = Paint()

    init {
        textPaint.color = Color.argb(40, 255, 255, 255)
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.style = Paint.Style.FILL
    }

    fun paintText(left: Float, top: Float, width: Float, height: Float, text: String, canvas: Canvas) {
        textPaint.textSize = height / 3f
        val textWidth = textPaint.measureText(text)

        val xPos = left - textWidth / 2f + width / 2f
        val yPos = top + height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        canvas.drawText(text, xPos, yPos, textPaint)
    }
}
