package com.swordfish.touchinput.radial

import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.google.android.material.R

object LemuroidTouchOverlayThemes {

    private const val LIGHTNESS = 0.5f
    private const val TEXT_LIGHTNESS = 1f
    private const val ALPHA_FOREGROUND = 0.75f
    private const val ALPHA_MIDDLE = 0.5f
    private const val ALPHA_BACKGROUND = 0.25f

    fun getGamePadTheme(view: View): RadialGamePadTheme {
        return buildTheme(view)
    }

    private fun buildTheme(view: View): RadialGamePadTheme {
        val colorOnSurface = MaterialColors.getColor(view, R.attr.colorOnSurface)
        val colorSurface = MaterialColors.getColor(view, R.attr.colorSurface)
        val colorPrimary = MaterialColors.getColor(view, R.attr.colorPrimary)
        val colorSecondary = MaterialColors.getColor(view, R.attr.colorSecondary)

        val mainColor = ColorUtils.blendARGB(colorSurface, colorOnSurface, LIGHTNESS)
        val mainTextColor = ColorUtils.blendARGB(colorSurface, colorOnSurface, TEXT_LIGHTNESS)

        return RadialGamePadTheme(
            normalColor = withAlpha(mainColor, ALPHA_MIDDLE),
            backgroundColor = withAlpha(mainColor, ALPHA_BACKGROUND),
            pressedColor = withAlpha(colorPrimary, ALPHA_FOREGROUND),
            textColor = withAlpha(mainTextColor, ALPHA_FOREGROUND),
            simulatedColor = withAlpha(colorSecondary, ALPHA_MIDDLE),
            lightColor = withAlpha(mainColor, ALPHA_BACKGROUND),
        )
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val alphaInt = (alpha * 255).toInt()
        return MaterialColors.compositeARGBWithAlpha(color, alphaInt)
    }
}
