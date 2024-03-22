package com.swordfish.lemuroid.common.graphics

import androidx.core.graphics.ColorUtils
import kotlin.math.abs

object ColorUtils {
    fun randomColor(
        seed: Any,
        paletteSize: Int = 128,
        saturation: Float = 0.5f,
        lightness: Float = 0.5f,
    ): Int {
        val hue = abs(seed.hashCode() % paletteSize) / paletteSize.toFloat() * 360f
        return ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
    }

    fun color(
        hue: Float,
        saturation: Float = 0.5f,
        lightness: Float = 0.5f,
    ): Int {
        return ColorUtils.HSLToColor(floatArrayOf(hue * 360f, saturation, lightness))
    }
}
