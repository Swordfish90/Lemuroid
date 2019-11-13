package com.swordfish.touchinput.interfaces

import com.swordfish.touchinput.data.ButtonEvent
import io.reactivex.Observable

interface ButtonEventsSource {
    fun getEvents(): Observable<ButtonEvent>
}
