package com.swordfish.touchinput.radial

import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.google.android.material.R

object LemuroidTouchOverlayThemes {

    private const val BASE_TRANSPARENCY = 0.8f

    fun getGamePadTheme(view: View): RadialGamePadTheme {
        val colorOnSurface = MaterialColors.getColor(view, R.attr.colorOnSurface)
        val colorSurface = MaterialColors.getColor(view, R.attr.colorSurface)
        val colorPrimary = MaterialColors.getColor(view, R.attr.colorPrimary)
        val colorSecondary = MaterialColors.getColor(view, R.attr.colorSecondary)
        val colorIntermediate = ColorUtils.blendARGB(colorSurface, colorOnSurface, 0.4f)

        return RadialGamePadTheme(
            normalColor = withAlpha(colorIntermediate, 0.8f),
            backgroundColor = withAlpha(colorIntermediate, 0.4f),
            pressedColor = withAlpha(colorPrimary, 1f),
            textColor = withAlpha(colorOnSurface, 1f),
            simulatedColor = withAlpha(colorSecondary, 0.8f),
            lightColor = withAlpha(colorIntermediate, 0.4f),
        )
    }

    fun getGamePadAlternate(view: View): RadialGamePadTheme {
        return getGamePadTheme(view)
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val alphaInt = (alpha * 255 * BASE_TRANSPARENCY).toInt()
        return MaterialColors.compositeARGBWithAlpha(color, alphaInt)
    }
}
