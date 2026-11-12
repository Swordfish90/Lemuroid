package com.swordfish.touchinput.radial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class OminoyPadTheme(
    private val appearance: Int = APPEARANCE_CLASSIC,
    /** The selected RGB color and overall opacity. */
    private val customColor: Color = Color(0xFF78AFFF),
) {
    private fun gray(luminosity: Float, opacity: Float) = Color(luminosity, luminosity, luminosity, opacity)

    private val isTransparent = appearance == APPEARANCE_TRANSPARENT
    private val isFrosted = appearance == APPEARANCE_FROSTED_WHITE
    private val isCustom = appearance == APPEARANCE_CUSTOM
    private val contrast = if (luminance(customColor) < 0.48f) Color.White else Color.Black

    val foregroundPadding: Dp = 8.dp
    val padding: Dp = 4.dp
    val outlineColor =
        when {
            isTransparent -> Color(0xFFD4D8E1).copy(alpha = 0.74f)
            isFrosted -> Color.Black.copy(alpha = 0.42f)
            isCustom -> contrast.copy(alpha = 0.50f)
            else -> Color.White.copy(alpha = 0.52f)
        }
    val outlineWidth = 1.25.dp
    val outlineHaloColor = Color(0xFF090A0D).copy(alpha = if (isTransparent) 0.90f else 0.58f)
    val outlineHaloWidth = 3.5.dp

    private val level3Fill = when {
        isTransparent -> Color.White.copy(alpha = 0.12f)
        isFrosted -> Color.White.copy(alpha = 0.42f)
        isCustom -> customColor.copy(alpha = customColor.alpha * 0.62f)
        else -> gray(1.0f, 0.18f)
    }
    private val level3FillPressed = when {
        isTransparent -> Color.White.copy(alpha = 0.28f)
        isFrosted -> Color.Black.copy(alpha = 0.16f)
        isCustom -> contrast.copy(alpha = customColor.alpha * 0.30f)
        else -> gray(1.0f, 0.34f)
    }
    val level3Shadow = if (isTransparent) Color.Black.copy(alpha = 0.10f) else DefaultShadowColor.copy(0.05f)
    val level3ShadowWidth = 4.dp

    private val level2Fill = when {
        isTransparent -> Color.White.copy(alpha = 0.10f)
        isFrosted -> Color.White.copy(alpha = 0.26f)
        isCustom -> customColor.copy(alpha = customColor.alpha * 0.42f)
        else -> gray(1.0f, 0.08f)
    }
    private val level2FillPressed = when {
        isTransparent -> Color.White.copy(alpha = 0.22f)
        isFrosted -> Color.Black.copy(alpha = 0.14f)
        isCustom -> contrast.copy(alpha = customColor.alpha * 0.25f)
        else -> gray(1.0f, 0.22f)
    }
    val level2Shadow = if (isTransparent) Color.Black.copy(alpha = 0.08f) else DefaultShadowColor.copy(0.05f)
    val level2ShadowWidth = 4.dp

    val level1Fill = when {
        isTransparent -> Color.White.copy(alpha = 0.06f)
        isFrosted -> Color.White.copy(alpha = 0.20f)
        isCustom -> customColor.copy(alpha = customColor.alpha * 0.28f)
        else -> gray(1.0f, 0.06f)
    }
    val level1Shadow = if (isTransparent) Color.Black.copy(alpha = 0.06f) else DefaultShadowColor.copy(0.10f)
    val level1ShadowWidth = 4.dp

    val level0CornerRadius = 0.dp
    // The controller panel must blend into the game letterbox/background.
    // Keep button fills and outlines above this base layer unchanged.
    val level0Fill = Color.Black
    val level0Shadow = if (isTransparent) Color.Black.copy(alpha = 0.06f) else DefaultShadowColor.copy(0.10f)
    val level0ShadowWidth = 2.dp

    fun compositeFill(pressed: Boolean): Color = if (pressed) level2FillPressed else level2Fill

    fun foregroundFill(pressed: Boolean): Color = if (pressed) level3FillPressed else level3Fill

    fun icons(pressed: Boolean): Color = when {
        appearance == APPEARANCE_CLASSIC -> Color.White.copy(alpha = if (pressed) 1f else 0.88f)
        isFrosted -> if (pressed) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.72f)
        isTransparent -> if (pressed) Color.White else Color.White.copy(alpha = 0.90f)
        isCustom -> contrast.copy(alpha = if (pressed) 0.96f else 0.84f)
        else -> Color.Black.copy(alpha = 0.50f)
    }

    private fun luminance(color: Color) =
        0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue

    companion object {
        const val APPEARANCE_CLASSIC = 0
        const val APPEARANCE_TRANSPARENT = 1
        const val APPEARANCE_FROSTED_WHITE = 2
        const val APPEARANCE_CUSTOM = 3
    }
}

val LocalOminoyPadTheme =
    compositionLocalOf<OminoyPadTheme> {
        error("OMINOYPadTheme is missing")
    }
