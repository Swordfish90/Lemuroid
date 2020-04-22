package com.swordfish.lemuroid.common.math

fun linearInterpolation(t: Float, a: Float, b: Float) = (a * (1.0f - t)) + (b * t)
