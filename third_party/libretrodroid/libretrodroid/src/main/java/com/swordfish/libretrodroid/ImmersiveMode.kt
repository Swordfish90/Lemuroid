package com.swordfish.libretrodroid

data class ImmersiveMode(
    val downscaledWidth: Int = 8,
    val downscaledHeight: Int = 8,
    val blurMaskSize: Int = 5,
    val blurBrightness: Float = 0.5F,
    val blurSkipUpdate: Int = 2,
    val blendFactor: Float = 0.1F
)
