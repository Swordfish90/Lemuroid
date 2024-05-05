package com.swordfish.lemuroid.common.math

import kotlin.math.roundToInt

data class Fraction(val numerator: Int, val denominator: Int) {
    val floatValue = numerator.toFloat() / denominator.toFloat()

    companion object {
        @JvmStatic
        fun fromValue(
            value: Float,
            denominator: Int,
        ): Fraction {
            return Fraction((value * denominator).roundToInt(), denominator)
        }
    }
}
