package com.swordfish.touchinput.radial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class LemuroidPadTheme {
    private fun gray(luminosity: Float, opacity: Float): Color {
        return Color(luminosity, luminosity, luminosity, opacity)
    }

    val padding: Dp = 4.dp

    private val icons = gray(0.0f, 0.50f)
    private val iconsPressed = gray(1.0f, 0.50f)

    private val level3Fill = gray(1.0f, 0.50f)
    private val level3FillPressed = gray(0.0f, 0.50f)
    private val level3Stroke = gray(1.0f, 0.20f)
    private val level3StrokePressed = gray(0.0f, 0.20f)
    val level3Shadow = DefaultShadowColor.copy(0.05f)
    val level3ShadowWidth = 4.dp
    val level3StrokeWidth = 1.dp

    private val level2Fill = gray(1.0f, 0.125f)
    private val level2FillPressed = gray(0.0f, 0.125f)
    private val level2Stroke = gray(1.0f, 0.10f)
    private val level2StrokePressed = gray(0.0f, 0.10f)
    val level2Shadow = DefaultShadowColor.copy(0.05f)
    val level2ShadowWidth = 4.dp
    val level2StrokeWidth = 1.dp

    val level1Fill = gray(1.0f, 0.10f)
    val level1Stroke = gray(1.0f, 0.10f)
    val level1Shadow = DefaultShadowColor.copy(0.10f)
    val level1ShadowWidth = 4.dp
    val level1StrokeWidth = 1.dp

    val level0CornerRadius = 20.dp
    val level0Fill = gray(1.0f, 0.05f)
    val level0Stroke = gray(1.0f, 0.05f)
    val level0Shadow = DefaultShadowColor.copy(0.10f)
    val level0ShadowWidth = 2.dp
    val level0StrokeWidth = 1.dp

    fun compositeFill(pressed: Boolean): Color {
        return if (pressed) {
            level2FillPressed
        } else {
            level2Fill
        }
    }

    fun compositeStroke(pressed: Boolean): Color {
        return if (pressed) {
            level2StrokePressed
        } else {
            level2Stroke
        }
    }

    fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) {
            level3FillPressed
        } else {
            level3Fill
        }
    }

    fun foregroundStroke(pressed: Boolean): Color {
        return if (pressed) {
            level3StrokePressed
        } else {
            level3Stroke
        }
    }

    fun icons(pressed: Boolean): Color {
        return if (pressed) {
            iconsPressed
        } else {
            icons
        }
    }
}

val LocalLemuroidPadTheme = compositionLocalOf<LemuroidPadTheme> {
    error("LemuroidPadTheme is missing")
}
