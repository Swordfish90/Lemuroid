package com.swordfish.touchinput.interfaces

import android.view.View
import com.swordfish.touchinput.events.PadBusEvent
import io.reactivex.Observable

interface PadBusSource {
    fun getBusEvents(): Observable<PadBusEvent>
    fun onBusEvent(event: PadBusEvent)
    fun getView(): View
}
