package com.swordfish.touchinput.interfaces

import com.swordfish.touchinput.events.ViewEvent
import io.reactivex.Observable

interface ButtonEventsSource {
    fun getEvents(): Observable<ViewEvent.Button>
}
