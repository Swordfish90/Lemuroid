package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidCompositeForeground(pressed: Boolean) {
    val theme = LocalLemuroidPadTheme.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2
        val borderWidth = theme.backgroundBorderWidth.toPx()

        drawCircle(
            color = theme.foregroundFill(pressed),
            radius = radius - borderWidth / 2
        )

        drawCircle(
            color = theme.foregroundStroke(pressed),
            radius = radius,
            style = Stroke(width = borderWidth)
        )
    }
}
