package com.swordfish.touchinput.events

import com.swordfish.touchinput.interfaces.StickEventsSource
import io.reactivex.ObservableTransformer
import kotlin.math.round

internal object EventsTransformers {
    fun actionButtonsMap(vararg keycodes: Int): ObservableTransformer<ViewEvent.Button, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.map { PadEvent.Button(it.action, keycodes[it.index]) }
        }
    }

    fun singleButtonMap(keycode: Int): ObservableTransformer<ViewEvent.Button, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.map { PadEvent.Button(it.action, keycode) }
        }
    }

    fun directionPadMap(): ObservableTransformer<ViewEvent.Stick, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.map {
                PadEvent.Stick(StickEventsSource.SOURCE_DPAD, round(it.xAxis), round(it.yAxis))
            }
        }
    }
}
