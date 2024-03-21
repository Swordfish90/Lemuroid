package com.swordfish.lemuroid.common.math

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun linearInterpolation(
    t: Float,
    a: Float,
    b: Float,
) = (a * (1.0f - t)) + (b * t)

object MathUtils {
    fun distance(
        x1: Float,
        x2: Float,
        y1: Float,
        y2: Float,
    ): Float {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))
    }

    fun convertPolarCoordinatesToSquares(
        angle: Float,
        strength: Float,
    ): PointF {
        val u = strength * cos(angle)
        val v = strength * sin(angle)
        return mapEllipticalDiskCoordinatesToSquare(u, v)
    }

    private fun mapEllipticalDiskCoordinatesToSquare(
        u: Float,
        v: Float,
    ): PointF {
        val u2 = u * u
        val v2 = v * v
        val twoSqrt2 = 2.0f * sqrt(2.0f)
        val subTermX = 2.0f + u2 - v2
        val subTermY = 2.0f - u2 + v2
        val termX1 = subTermX + u * twoSqrt2
        val termX2 = subTermX - u * twoSqrt2
        val termY1 = subTermY + v * twoSqrt2
        val termY2 = subTermY - v * twoSqrt2

        val x = (0.5f * sqrt(termX1) - 0.5f * sqrt(termX2))
        val y = (0.5f * sqrt(termY1) - 0.5f * sqrt(termY2))

        return PointF(x, y)
    }

    const val PI2 = 2 * Math.PI.toFloat()
}
