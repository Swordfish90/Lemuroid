package com.swordfish.touchinput.interfaces

import com.swordfish.touchinput.events.ViewEvent
import io.reactivex.Observable

interface ClickEventsSource {
    fun getEvents(): Observable<ViewEvent.Click>
}
