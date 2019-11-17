package com.swordfish.touchinput.events

interface HapticEvent {
    val haptic: Boolean
}

sealed class PadEvent: HapticEvent {
    data class Button(
        val action: Int,
        val keycode: Int,
        override val haptic: Boolean
    ): PadEvent(), HapticEvent

    data class Stick(
        val source: Int,
        val xAxis: Float,
        val yAxis: Float,
        override val haptic: Boolean
    ): PadEvent(), HapticEvent
}
