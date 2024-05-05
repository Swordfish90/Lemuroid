package com.swordfish.lemuroid.app.mobile.feature.tilt

import com.swordfish.radialgamepad.library.RadialGamePad

interface TiltTracker {
    fun updateTracking(
        xTilt: Float,
        yTilt: Float,
        pads: Sequence<RadialGamePad>,
    )

    fun stopTracking(pads: Sequence<RadialGamePad>)

    fun trackedIds(): Set<Int>
}
