package com.swordfish.touchinput.radial

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class LemuroidPadTheme(colorScheme: ColorScheme) {
    private fun color(luminosity: Float, opacity: Float): Color {
        return Color(luminosity, luminosity, luminosity, opacity)
    }

    val padding: Dp = 4.dp

    private val icons = color(0.0f, 0.50f)
    private val iconsPressed = color(1.0f, 0.50f)

    private val foregroundFill = color(1.0f, 0.50f)
    private val foregroundFillPressed = color(0.0f, 0.40f)
    val foregroundShadow = DefaultShadowColor.copy(0.05f)
    val foregroundShadowWidth = 4.dp

    val backgroundFill = color(1.0f, 0.10f)
    val backgroundShadow = DefaultShadowColor.copy(0.10f)
    val backgroundShadowWidth = 4.dp

    fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) {
            foregroundFillPressed
        } else {
            foregroundFill
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
