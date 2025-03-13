package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TranslucentSurface(
    modifier: Modifier = Modifier,
    fillColor: Color = Color.Transparent,
    shadowColor: Color = Color.Transparent,
    shadowWidth: Dp = Dp.Hairline,
    scale: Float = 1.0f,
    content: @Composable BoxWithConstraintsScope.() -> Unit = { },
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val canvasSize = maxWidth * scale

        Spacer(
            modifier = Modifier
                .size(canvasSize)
                .drawWithCache {
                    val radius = size.minDimension / 2
                    val epsilon = 1.dp.toPx() / radius
                    val shadowPx = shadowWidth.toPx()

                    val adjustedFillColor = fillColor.compositeOver(shadowColor)

                    val gradientEndFill = 1f - (shadowPx / radius)
                    val gradientStartShadow = gradientEndFill + epsilon

                    val colorStops = buildList {
                        add(0f to adjustedFillColor)
                        add(gradientEndFill to adjustedFillColor)

                        if (shadowPx > 0) {
                            add(gradientStartShadow to shadowColor)
                        }

                        add(1f to Color.Transparent)
                    }.toTypedArray()

                    val brush = Brush.radialGradient(colorStops = colorStops)
                    onDrawWithContent {
                        drawCircle(brush = brush, radius = radius)
                    }
                }
        )

        content()
    }
}
