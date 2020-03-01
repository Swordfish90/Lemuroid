package com.swordfish.touchinput.events

sealed class ViewEvent {
    data class Button(val action: Int, val index: Int, val haptic: Boolean)
    data class Stick(val xAxis: Float, val yAxis: Float, val haptic: Boolean)
}
