package com.swordfish.touchinput.interfaces

import com.swordfish.touchinput.events.ViewEvent
import io.reactivex.Observable

interface StickEventsSource {
    fun getEvents(): Observable<ViewEvent.Stick>

    companion object {
        const val SOURCE_DPAD = 0
        const val SOURCE_LEFT_STICK = 1
        const val SOURCE_RIGHT_STICK = 2
    }
}
