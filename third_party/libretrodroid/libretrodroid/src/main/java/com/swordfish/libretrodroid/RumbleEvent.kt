package com.swordfish.libretrodroid

/**
 * Represents a rumble event. Libretro exposes two independently controlled motors. The strength
 * value of both is represented in range [0, 1.0]. */
data class RumbleEvent(val port: Int, val strengthWeak: Float, val strengthStrong: Float)
