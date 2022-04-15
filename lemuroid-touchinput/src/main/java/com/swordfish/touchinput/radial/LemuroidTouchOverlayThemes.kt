package com.swordfish.touchinput.radial

import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.google.android.material.R

object LemuroidTouchOverlayThemes {

    private const val ALPHA_FOREGROUND = 0.75f
    private const val ALPHA_MIDDLE = 0.5f
    private const val ALPHA_BACKGROUND = 0.25f

    private data class Palette(
        val foreground: Int,
        val normal: Int,
        val background: Int,
        val primary: Int,
        val secondary: Int
    )

    fun getGamePadTheme(view: View): RadialGamePadTheme {
        val colorSurface = MaterialColors.getColor(view, R.attr.colorSurface)
        val palette = buildDefaultPalette(view)

        // When in overlay the transparent control is drawn on black while here on colorSurface.
        // Adjusting the background color we're able to preserve the contrast in the two modes.
        val adjustedPalette = palette.copy(
            background = ColorUtils.blendARGB(colorSurface, palette.normal, ALPHA_MIDDLE)
        )
        return buildTheme(adjustedPalette)
    }

    fun getGamePadOverlayTheme(view: View): RadialGamePadTheme {
        return buildTheme(buildDefaultPalette(view))
    }

    private fun buildDefaultPalette(view: View): Palette {
        val colorOnSurface = MaterialColors.getColor(view, R.attr.colorOnSurface)
        val colorSurface = MaterialColors.getColor(view, R.attr.colorSurface)
        val colorPrimary = MaterialColors.getColor(view, R.attr.colorPrimary)
        val colorSecondary = MaterialColors.getColor(view, R.attr.colorSecondary)
        val colorIntermediate = ColorUtils.blendARGB(colorSurface, colorOnSurface, 0.5f)

        return Palette(
            colorOnSurface,
            colorIntermediate,
            colorIntermediate,
            colorPrimary,
            colorSecondary
        )
    }

    private fun buildTheme(palette: Palette): RadialGamePadTheme {
        return RadialGamePadTheme(
            normalColor = withAlpha(palette.normal, ALPHA_MIDDLE),
            backgroundColor = withAlpha(palette.background, ALPHA_BACKGROUND),
            pressedColor = withAlpha(palette.primary, ALPHA_FOREGROUND),
            textColor = withAlpha(palette.foreground, ALPHA_FOREGROUND),
            simulatedColor = withAlpha(palette.secondary, ALPHA_MIDDLE),
            lightColor = withAlpha(palette.normal, ALPHA_BACKGROUND),
        )
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val alphaInt = (alpha * 255).toInt()
        return MaterialColors.compositeARGBWithAlpha(color, alphaInt)
    }
}
