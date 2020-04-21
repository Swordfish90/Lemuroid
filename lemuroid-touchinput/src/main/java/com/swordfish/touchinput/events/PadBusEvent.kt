package com.swordfish.touchinput.events

sealed class PadBusEvent(val sourceId: Int) {
    class TiltEnabled(sourceId: Int): PadBusEvent(sourceId)
    class TiltDisabled(sourceId: Int): PadBusEvent(sourceId)
    object OnPause: PadBusEvent(0)
}
