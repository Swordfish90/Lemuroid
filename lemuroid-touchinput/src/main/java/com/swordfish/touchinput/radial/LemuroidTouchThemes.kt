package com.swordfish.touchinput.radial

import android.content.Context
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.touchinput.controller.R
import kotlin.math.roundToInt

object LemuroidTouchThemes {

    private const val PRESSED_COLOR_ALPHA = 0.5f

    fun getGamePadTheme(accentColor: Int, context: Context): RadialGamePadTheme {
        val accentColorComponents = GraphicsUtils.colorToRgb(accentColor)
        val alpha = (255 * PRESSED_COLOR_ALPHA).roundToInt()
        val pressedColor = GraphicsUtils.rgbaToColor(accentColorComponents + listOf(alpha))
        val simulatedColor = GraphicsUtils.rgbaToColor(accentColorComponents + (255 * 0.25f).roundToInt())
        return RadialGamePadTheme(
            normalColor = context.getColor(R.color.touch_control_normal),
            pressedColor = pressedColor,
            simulatedColor = simulatedColor,
            primaryDialBackground = context.getColor(R.color.touch_control_background),
            textColor = context.getColor(R.color.touch_control_text),
            enableStroke = true,
            strokeColor = context.getColor(R.color.touch_control_stroke),
            strokeLightColor = context.getColor(R.color.touch_control_stroke_light),
            strokeWidthDp = context.resources.getInteger(R.integer.touch_control_stroke_size_int).toFloat()
        )
    }

    fun getMenuTheme(accentColor: Int, context: Context): RadialGamePadTheme {
        return getGamePadTheme(accentColor, context).copy(
            normalColor = context.getColor(R.color.touch_control_dark_background),
            strokeColor = context.getColor(R.color.touch_control_dark_stroke)
        )
    }
}
