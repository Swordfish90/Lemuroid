package com.swordfish.touchinput.radial.ui

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DrawPaddingValues(
    val left: Dp = 0.dp,
    val top: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp
)

private object ShadowCache {
    private val bitmapCache = mutableMapOf<String, ImageBitmap>()

    fun getOrCreate(
        width: Int,
        height: Int,
        cornerRadius: Float,
        shadowColor: Color,
        blurRadius: Float,
    ): ImageBitmap {
        val key = "$width-$height-$cornerRadius-${shadowColor.toArgb()}-$blurRadius"
        return bitmapCache.getOrPut(key) {
            val bitmap = ImageBitmap(width, height)
            val canvas = Canvas(bitmap)

            val frameworkPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = shadowColor.toArgb()
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }

            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                width.toFloat() + 0f,
                height.toFloat() + 0f,
                cornerRadius,
                cornerRadius,
                frameworkPaint
            )

            bitmap
        }
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dp.Infinity,
    fillColor: Color = Color.White.copy(alpha = 0.15f),
    strokeColor: Color = Color.White.copy(alpha = 0.3f),
    shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    strokeWidth: Dp = 1.dp,
    shadowWidth: Dp = 1.dp,
    scale: Float = 1.0f,
    drawPadding: DrawPaddingValues = DrawPaddingValues(),
    content: @Composable BoxWithConstraintsScope.() -> Unit = { }
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.drawWithCache {
            val strokePx = strokeWidth.toPx()
            val blurRadiusPx = shadowWidth.toPx()

            val paddingStart = drawPadding.left.toPx()
            val paddingTop = drawPadding.top.toPx()
            val paddingEnd = drawPadding.right.toPx()
            val paddingBottom = drawPadding.bottom.toPx()

            val expandedWidth = size.width - paddingStart - paddingEnd
            val expandedHeight = size.height - paddingTop - paddingBottom
            val expandedSize = Size(expandedWidth, expandedHeight)

            val cornerRadiusPx = minOf(cornerRadius.toPx(), expandedSize.minDimension / 2f)

            val shadowBitmap = ShadowCache.getOrCreate(
                width = expandedSize.width.toInt(),
                height = expandedSize.height.toInt(),
                cornerRadius = cornerRadiusPx,
                shadowColor = shadowColor,
                blurRadius = blurRadiusPx
            )

            val scaledSize = expandedSize * scale
            val centerOffset = (expandedSize - scaledSize) * 0.5f

            val strokeInset = strokePx / 2f
            val fillSize = scaledSize + Size(strokePx, strokePx)
            val fillOffset = centerOffset - Offset(strokeInset, strokeInset)

            val drawOffset = Offset(paddingStart, paddingTop)
            val adjustedRadius = cornerRadiusPx + strokeInset

            onDrawWithContent {
                drawImage(shadowBitmap, topLeft = drawOffset)

                drawRoundRect(
                    color = fillColor,
                    topLeft = fillOffset + drawOffset,
                    size = fillSize,
                    cornerRadius = CornerRadius(adjustedRadius, adjustedRadius)
                )

                drawRoundRect(
                    color = strokeColor,
                    topLeft = centerOffset + drawOffset,
                    size = scaledSize,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokePx)
                )

                drawContent()
            }
        },
        content = content
    )
}

private operator fun Size.minus(scaledSize: Size): Offset {
    return Offset(this.width - scaledSize.width, this.height - scaledSize.height)
}

private operator fun Size.plus(scaledSize: Size): Size {
    return Size(this.width + scaledSize.width, this.height + scaledSize.height)
}
