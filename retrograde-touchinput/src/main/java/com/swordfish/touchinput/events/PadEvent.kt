package com.swordfish.touchinput.events

sealed class PadEvent {
    data class Button(val action: Int, val keycode: Int): PadEvent()
    data class Stick(val source: Int, val xAxis: Float, val yAxis: Float): PadEvent()
}
