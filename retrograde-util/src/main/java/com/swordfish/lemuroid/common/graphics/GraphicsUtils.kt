package com.swordfish.lemuroid.common.graphics

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

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

    fun convertDpToPixel(
        dp: Float,
        context: Context,
    ): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(
        px: Float,
        context: Context,
    ): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getRawDpSize(
        context: Context,
        resource: Int,
    ): Float {
        val value = TypedValue()
        context.resources.getValue(resource, value, true)
        return TypedValue.complexToFloat(value.data)
    }

    fun rotatePoint(
        offset: Offset,
        angleInDegrees: Double,
    ): Offset {
        val angle = Math.toRadians(angleInDegrees)
        val cosTheta = cos(angle)
        val sinTheta = sin(angle)
        val xNew = offset.x * cosTheta - offset.y * sinTheta
        val yNew = offset.x * sinTheta + offset.y * cosTheta
        return Offset(xNew.toFloat(), yNew.toFloat())
    }
}
