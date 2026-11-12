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
import kotlin.math.ceil

private object ShadowCache {
    private val bitmapCache = mutableMapOf<String, ImageBitmap>()

    fun getOrCreate(
        width: Int,
        height: Int,
        cornerRadius: Float,
        shadowColor: Color,
        blurRadius: Float,
        padding: Int,
    ): ImageBitmap {
        val key = "$width-$height-$cornerRadius-${shadowColor.toArgb()}-$blurRadius-$padding"
        return bitmapCache.getOrPut(key) {
            val bitmapWidth = width + padding * 2
            val bitmapHeight = height + padding * 2
            val bitmap = ImageBitmap(bitmapWidth, bitmapHeight)
            val canvas = Canvas(bitmap)

            val frameworkPaint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = shadowColor.toArgb()
                    maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
                }

            canvas.nativeCanvas.drawRoundRect(
                padding.toFloat(),
                padding.toFloat(),
                padding.toFloat() + width.toFloat(),
                padding.toFloat() + height.toFloat(),
                cornerRadius,
                cornerRadius,
                frameworkPaint,
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
    shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    shadowWidth: Dp = 1.dp,
    outlineColor: Color = Color.Transparent,
    outlineWidth: Dp = 0.dp,
    outlineHaloColor: Color = Color.Transparent,
    outlineHaloWidth: Dp = 0.dp,
    content: @Composable BoxWithConstraintsScope.() -> Unit = { },
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier =
            modifier.drawWithCache {
                val blurRadiusPx = shadowWidth.toPx()

                val expandedWidth = size.width
                val expandedHeight = size.height

                if (expandedWidth <= 0f || expandedHeight <= 0f) {
                    return@drawWithCache onDrawWithContent {
                        drawContent()
                    }
                }

                val expandedSize = Size(expandedWidth, expandedHeight)
                val cornerRadiusPx = minOf(cornerRadius.toPx(), expandedSize.minDimension / 2f)
                val shouldDrawShadow = shadowColor.alpha > 0f && blurRadiusPx > 0f
                val shadowPaddingPx = if (shouldDrawShadow) blurRadiusPx else 0f
                val shadowPadding = ceil(shadowPaddingPx).toInt().coerceAtLeast(0)

                val shadowBitmap =
                    if (shouldDrawShadow) {
                        ShadowCache.getOrCreate(
                            width = expandedSize.width.toInt().coerceAtLeast(1),
                            height = expandedSize.height.toInt().coerceAtLeast(1),
                            cornerRadius = cornerRadiusPx,
                            shadowColor = shadowColor,
                            blurRadius = blurRadiusPx,
                            padding = shadowPadding,
                        )
                    } else {
                        null
                    }

                val haloWidthPx = outlineHaloWidth.toPx().coerceAtMost(expandedSize.minDimension * 0.24f)
                val outlineWidthPx = outlineWidth.toPx().coerceAtMost(expandedSize.minDimension * 0.16f)
                onDrawWithContent {
                    if (shadowBitmap != null) {
                        val shadowOffset = -Offset(shadowPadding.toFloat(), shadowPadding.toFloat())
                        drawImage(shadowBitmap, topLeft = shadowOffset)
                    }

                    drawRoundRect(
                        color = fillColor,
                        topLeft = Offset(0f, 0f),
                        size = expandedSize,
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    )

                    if (haloWidthPx > 0f && outlineHaloColor.alpha > 0f) {
                        val inset = haloWidthPx / 2f
                        val innerSize = Size(
                            (expandedSize.width - haloWidthPx).coerceAtLeast(0f),
                            (expandedSize.height - haloWidthPx).coerceAtLeast(0f),
                        )
                        if (innerSize.width > 0f && innerSize.height > 0f) {
                            drawRoundRect(
                                color = outlineHaloColor,
                                topLeft = Offset(inset, inset),
                                size = innerSize,
                                cornerRadius = CornerRadius(
                                    (cornerRadiusPx - inset).coerceAtLeast(0f),
                                    (cornerRadiusPx - inset).coerceAtLeast(0f),
                                ),
                                style = Stroke(width = haloWidthPx),
                            )
                        }
                    }

                    if (outlineWidthPx > 0f && outlineColor.alpha > 0f) {
                        val inset = haloWidthPx + outlineWidthPx / 2f
                        val innerSize = Size(
                            (expandedSize.width - inset * 2f).coerceAtLeast(0f),
                            (expandedSize.height - inset * 2f).coerceAtLeast(0f),
                        )
                        if (innerSize.width > 0f && innerSize.height > 0f) {
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(inset, inset),
                                size = innerSize,
                                cornerRadius = CornerRadius(
                                    (cornerRadiusPx - inset).coerceAtLeast(0f),
                                    (cornerRadiusPx - inset).coerceAtLeast(0f),
                                ),
                                style = Stroke(width = outlineWidthPx),
                            )
                        }
                    }

                    drawContent()
                }
            },
        content = content,
    )
}
