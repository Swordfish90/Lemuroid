package com.swordfish.touchinput.radial

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class LemuroidPadTheme(colorScheme: ColorScheme) {
    private val foregroundFill: Color = colorScheme.surfaceContainerHighest.copy(0.5f)
    private val foregroundStroke: Color = colorScheme.onSurface.copy(0.75f)
    private val foregroundFillPressed: Color = colorScheme.onSurface.copy(0.75f)
    private val foregroundStrokePressed: Color = colorScheme.surfaceContainerHighest.copy(0.75f)
    val padding: Dp = 4.dp
    val backgroundBorderWidth: Dp = 1.dp
    val foregroundBorderWidth: Dp = 2.dp
    val backgroundFill: Color = colorScheme.surfaceContainerHighest.copy(alpha = 0.25f)
    val backgroundStroke: Color = colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) {
            foregroundFillPressed
        } else {
            foregroundFill
        }
    }

    fun foregroundStroke(pressed: Boolean): Color {
        return if (pressed) {
            foregroundStrokePressed
        } else {
            foregroundStroke
        }
    }
}

val LocalLemuroidPadTheme = compositionLocalOf<LemuroidPadTheme> {
    error("LemuroidPadTheme is missing")
}
