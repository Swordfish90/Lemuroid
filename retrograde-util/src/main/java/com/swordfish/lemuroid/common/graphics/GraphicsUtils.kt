package com.swordfish.lemuroid.common.graphics

import android.graphics.Color

object GraphicsUtils {

    fun colorToRgb(color: Int): List<Int> {
        return colorToRgba(color).take(3)
    }

    fun colorToRgba(color: Int): List<Int> {
        return listOf(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color))
    }

    fun rgbaToColor(rgbaColor: List<Int>): Int {
        return Color.argb(rgbaColor[3], rgbaColor[0], rgbaColor[1], rgbaColor[2])
    }
}
