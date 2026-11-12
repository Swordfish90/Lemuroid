package com.swordfish.lemuroid.common.drawable

import android.graphics.Canvas
import android.graphics.Paint

class CompactCoverPlaceholderDrawable(
    private val title: String,
    backgroundColor: Int,
) : BaseCoverPlaceholderDrawable(backgroundColor) {
    override fun drawContent(canvas: Canvas) {
        useTextPaint()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = bounds.height() * TEXT_SIZE_FRACTION

        val contentLeft = bounds.left + bounds.width() * SPINE_WIDTH_FRACTION
        canvas.drawText(
            title,
            (contentLeft + bounds.right) / 2f,
            centeredBaseline(bounds.exactCenterY()),
            paint,
        )
    }

    private companion object {
        const val TEXT_SIZE_FRACTION = 0.3f
    }
}
