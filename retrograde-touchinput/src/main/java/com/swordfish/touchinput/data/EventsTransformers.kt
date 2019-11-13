package com.swordfish.touchinput.data

import android.view.KeyEvent
import com.swordfish.touchinput.utils.observableOf
import com.swordfish.touchinput.views.DirectionPad
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import java.security.InvalidParameterException

internal object EventsTransformers {
    fun actionButtonsMap(vararg keycodes: Int): ObservableTransformer<ButtonEvent, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.map { PadEvent(it.action, keycodes[it.index]) }
        }
    }

    fun singleButtonMap(keycode: Int): ObservableTransformer<ButtonEvent, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.map { PadEvent(it.action, keycode) }
        }
    }

    fun directionPadMap(): ObservableTransformer<ButtonEvent, PadEvent> {
        return ObservableTransformer { upstream ->
            upstream.flatMap { buttonEvent ->
                mapDirectionToKey(buttonEvent.index).map { PadEvent(buttonEvent.action, it) }
            }
        }
    }

    private fun mapDirectionToKey(index: Int): Observable<Int> {
        return when (index) {
            DirectionPad.BUTTON_LEFT -> observableOf(KeyEvent.KEYCODE_DPAD_LEFT)
            DirectionPad.BUTTON_RIGHT -> observableOf(KeyEvent.KEYCODE_DPAD_RIGHT)
            DirectionPad.BUTTON_UP -> observableOf(KeyEvent.KEYCODE_DPAD_UP)
            DirectionPad.BUTTON_DOWN -> observableOf(KeyEvent.KEYCODE_DPAD_DOWN)
            DirectionPad.BUTTON_UP_LEFT -> observableOf(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP)
            DirectionPad.BUTTON_UP_RIGHT -> observableOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_RIGHT)
            DirectionPad.BUTTON_DOWN_LEFT -> observableOf(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)
            DirectionPad.BUTTON_DOWN_RIGHT -> observableOf(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
            else -> throw InvalidParameterException("Invalid direction event with index: $index")
        }
    }
}
