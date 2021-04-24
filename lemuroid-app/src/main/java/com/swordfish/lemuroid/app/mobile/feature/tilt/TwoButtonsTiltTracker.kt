package com.swordfish.lemuroid.app.mobile.feature.tilt

import com.swordfish.radialgamepad.library.RadialGamePad

class TwoButtonsTiltTracker(private val leftId: Int, private val rightId: Int) : TiltTracker {

    override fun updateTracking(xTilt: Float, yTilt: Float, pads: Sequence<RadialGamePad>) {
        pads.forEach {
            it.simulateKeyEvent(leftId, xTilt < 0.5 - ACTIVATION_THRESHOLD)
            it.simulateKeyEvent(rightId, xTilt > 0.5 + ACTIVATION_THRESHOLD)
        }
    }

    override fun stopTracking(pads: Sequence<RadialGamePad>) {
        pads.forEach {
            it.simulateClearKeyEvent(leftId)
            it.simulateClearKeyEvent(rightId)
        }
    }

    override fun trackedIds(): Set<Int> = setOf(leftId, rightId)

    companion object {
        private const val ACTIVATION_THRESHOLD = 0.25
    }
}
