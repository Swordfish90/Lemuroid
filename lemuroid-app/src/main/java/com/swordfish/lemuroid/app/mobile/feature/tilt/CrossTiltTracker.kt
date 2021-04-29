package com.swordfish.lemuroid.app.mobile.feature.tilt

import com.swordfish.lemuroid.common.math.MathUtils
import com.swordfish.radialgamepad.library.RadialGamePad

class CrossTiltTracker(val id: Int) : TiltTracker {

    override fun updateTracking(
        xTilt: Float,
        yTilt: Float,
        pads: Sequence<RadialGamePad>
    ) {
        if (MathUtils.distance(xTilt, 0.5f, yTilt, 0.5f) > ACTIVATION_THRESHOLD) {
            pads.forEach { it.simulateMotionEvent(id, xTilt, yTilt) }
        } else if (MathUtils.distance(xTilt, 0.5f, yTilt, 0.5f) < DEACTIVATION_THRESHOLD) {
            pads.forEach { it.simulateMotionEvent(id, 0.5f, 0.5f) }
        }
    }

    override fun trackedIds(): Set<Int> = setOf(id)

    override fun stopTracking(pads: Sequence<RadialGamePad>) {
        pads.forEach { it.simulateClearMotionEvent(id) }
    }

    companion object {
        private const val ACTIVATION_THRESHOLD = 0.25f
        private const val DEACTIVATION_THRESHOLD = 0.225f
    }
}
