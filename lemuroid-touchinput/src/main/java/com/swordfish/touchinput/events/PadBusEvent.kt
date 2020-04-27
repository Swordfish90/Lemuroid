package com.swordfish.touchinput.events

sealed class PadBusEvent(val sourceId: Int) {
    class TiltEnabled(sourceId: Int) : PadBusEvent(sourceId)
    class TiltDisabled(sourceId: Int) : PadBusEvent(sourceId)
    class SetTiltSensitivity(val tiltSensitivity: Float) : PadBusEvent(0)
    object OnPause : PadBusEvent(0)
    object OnResume : PadBusEvent(0)
}
