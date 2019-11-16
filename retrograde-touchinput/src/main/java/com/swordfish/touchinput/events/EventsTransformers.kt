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

    fun directionPadMap() = stickMap(StickEventsSource.SOURCE_DPAD)

    fun leftStickMap() = stickMap(StickEventsSource.SOURCE_LEFT_STICK)

    fun rightStickMap() = stickMap(StickEventsSource.SOURCE_RIGHT_STICK)

    private fun stickMap(eventSource: Int): ObservableTransformer<ViewEvent.Stick, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.map {
                PadEvent.Stick(eventSource, it.xAxis, it.yAxis)
            }
        }
    }
}
