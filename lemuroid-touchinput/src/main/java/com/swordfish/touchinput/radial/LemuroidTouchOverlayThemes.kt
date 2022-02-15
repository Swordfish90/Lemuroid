package com.swordfish.touchinput.radial

import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.google.android.material.R

object LemuroidTouchOverlayThemes {

    private const val BASE_TRANSPARENCY = 0.6f

    fun getGamePadTheme(view: View): RadialGamePadTheme {
        val colorOnSurface = MaterialColors.getColor(view, R.attr.colorOnSurface)
        val colorSurface = MaterialColors.getColor(view, R.attr.colorSurface)
        val colorPrimary = MaterialColors.getColor(view, R.attr.colorPrimary)
        val colorIntermediate = ColorUtils.blendARGB(colorSurface, colorOnSurface, 0.5f)

        return RadialGamePadTheme(
            normalColor = withAlpha(colorIntermediate, 0.75f),
            primaryDialBackground = withAlpha(colorIntermediate, 0.25f),
            pressedColor = withAlpha(colorPrimary, 1f),
            textColor = withAlpha(colorOnSurface, 1f),
            simulatedColor = withAlpha(colorPrimary, 0.75f),
            lightColor = withAlpha(colorOnSurface, 0.25f),
        )
    }

    fun getGamePadAlternate(view: View): RadialGamePadTheme {
        val colorOnSurface = MaterialColors.getColor(view, R.attr.colorOnSurface)
        val colorSurface = MaterialColors.getColor(view, R.attr.colorSurface)
        val colorPrimary = MaterialColors.getColor(view, R.attr.colorPrimary)
        val colorIntermediate = ColorUtils.blendARGB(colorSurface, colorOnSurface, 0.25f)

        return RadialGamePadTheme(
            normalColor = withAlpha(colorIntermediate, 0.75f),
            primaryDialBackground = withAlpha(colorIntermediate, 0.25f),
            pressedColor = withAlpha(colorPrimary, 1f),
            textColor = withAlpha(colorOnSurface, 1f),
            simulatedColor = withAlpha(colorPrimary, 0.75f),
            lightColor = withAlpha(colorOnSurface, 0.25f),
        )
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val alphaInt = (alpha * 255 * BASE_TRANSPARENCY).toInt()
        return MaterialColors.compositeARGBWithAlpha(color, alphaInt)
    }
}
