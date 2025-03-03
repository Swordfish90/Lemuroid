package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidControlBackground(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current
    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.minDimension / 2
        val borderWidth = theme.backgroundBorderWidth.toPx() // Convert DP to pixels

        drawCircle(
            color = theme.backgroundFill,
            radius = radius - borderWidth / 2
        )

        drawCircle(
            color = theme.backgroundStroke,
            radius = radius,
            style = Stroke(width = borderWidth)
        )
    }
}
