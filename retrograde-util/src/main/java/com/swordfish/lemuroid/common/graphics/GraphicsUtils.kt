package com.swordfish.lemuroid.common.graphics

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics

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

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
