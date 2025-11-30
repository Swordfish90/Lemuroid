package com.swordfish.lemuroid.common.math

import kotlin.math.sin

fun computeSizeOfItemsAroundCircumference(itemsCount: Int): Float {
    val sinValue = sin(Math.PI / itemsCount).toFloat()
    return sinValue / (1f - sinValue)
}
