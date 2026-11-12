package com.swordfish.lemuroid.common.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout

class DetailedCoverPlaceholderDrawable(
    private val title: String,
    private val platform: String,
    private val metadata: String,
    backgroundColor: Int,
) : BaseCoverPlaceholderDrawable(backgroundColor) {
    private var titleLayout: StaticLayout? = null
    private var titleTextSize = 0f

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        titleLayout = null
    }

    override fun drawContent(canvas: Canvas) {
        val contentLeft = bounds.left + bounds.width() * HORIZONTAL_PADDING_FRACTION
        val contentWidth = bounds.width() * CONTENT_WIDTH_FRACTION

        drawPlatform(canvas)
        drawTitle(canvas, contentLeft, contentWidth.toInt().coerceAtLeast(1))
        drawMetadata(canvas, contentLeft, contentWidth)
    }

    private fun drawPlatform(canvas: Canvas) {
        useTextPaint()
        paint.textAlign = Paint.Align.CENTER
        fitSingleLine(
            text = platform,
            textSize = bounds.width() * PLATFORM_TEXT_SIZE_FRACTION,
            maxWidth = bounds.height() * PLATFORM_WIDTH_FRACTION,
        )

        val centerX = bounds.left + bounds.width() * SPINE_CENTER_X_FRACTION
        val centerY = bounds.exactCenterY()
        val saveCount = canvas.save()
        canvas.rotate(-90f, centerX, centerY)
        canvas.drawText(platform, centerX, centeredBaseline(centerY), paint)
        canvas.restoreToCount(saveCount)
    }

    private fun drawTitle(canvas: Canvas, contentLeft: Float, contentWidth: Int) {
        useTextPaint()
        paint.textAlign = Paint.Align.LEFT
        val layout = titleLayout ?: createTitleLayout(contentWidth).also { titleLayout = it }
        paint.textSize = titleTextSize

        val saveCount = canvas.save()
        canvas.translate(contentLeft, bounds.top + bounds.height() * TITLE_TOP_FRACTION)
        layout.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun createTitleLayout(width: Int): StaticLayout {
        paint.textSize = bounds.height() * TITLE_TEXT_SIZE_FRACTION
        val maximumHeight = bounds.height() * TITLE_HEIGHT_FRACTION
        var layout = buildTitleLayout(width)
        while (layout.height > maximumHeight && paint.textSize > MINIMUM_TEXT_SIZE) {
            paint.textSize *= TEXT_SIZE_REDUCTION_FACTOR
            layout = buildTitleLayout(width)
        }
        titleTextSize = paint.textSize
        return layout
    }

    private fun drawMetadata(canvas: Canvas, contentLeft: Float, contentWidth: Float) {
        if (metadata.isEmpty()) return

        useTextPaint(DIVIDER_ALPHA)
        val dividerTop = bounds.top + bounds.height() * DIVIDER_TOP_FRACTION
        canvas.drawRect(contentLeft, dividerTop, contentLeft + contentWidth, dividerTop + 1f, paint)

        useTextPaint()
        fitSingleLine(
            text = metadata,
            textSize = bounds.height() * LABEL_TEXT_SIZE_FRACTION,
            maxWidth = contentWidth,
        )
        canvas.drawText(
            metadata,
            contentLeft,
            bounds.top + bounds.height() * METADATA_BASELINE_FRACTION,
            paint,
        )
    }

    private fun buildTitleLayout(width: Int) =
        StaticLayout.Builder.obtain(title, 0, title.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .setLineSpacing(0f, LINE_SPACING_MULTIPLIER)
            .build()

    private companion object {
        const val HORIZONTAL_PADDING_FRACTION = 0.275f
        const val CONTENT_WIDTH_FRACTION = 0.6f
        const val PLATFORM_TEXT_SIZE_FRACTION = 0.052f
        const val PLATFORM_WIDTH_FRACTION = 0.82f
        const val LABEL_TEXT_SIZE_FRACTION = 0.035f
        const val TITLE_TOP_FRACTION = 0.22f
        const val TITLE_HEIGHT_FRACTION = 0.5f
        const val TITLE_TEXT_SIZE_FRACTION = 0.075f
        const val DIVIDER_TOP_FRACTION = 0.8f
        const val DIVIDER_ALPHA = 90
        const val METADATA_BASELINE_FRACTION = 0.87f
        const val MINIMUM_TEXT_SIZE = 1f
        const val TEXT_SIZE_REDUCTION_FACTOR = 0.9f
        const val LINE_SPACING_MULTIPLIER = 0.95f
    }
}
