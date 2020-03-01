package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.EventsTransformers
import com.swordfish.touchinput.events.OptionType
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.views.ActionButtons
import com.swordfish.touchinput.views.DirectionPad
import com.swordfish.touchinput.views.IconButton
import com.swordfish.touchinput.views.LargeSingleButton
import com.swordfish.touchinput.views.SmallSingleButton
import io.reactivex.Observable

class NDSPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseGamePad(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_nds, this)
    }

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(listOf(
            getStartEvent(),
            getSelectEvent(),
            getDirectionEvents(),
            getActionEvents(),
            getR1Events(),
            getL1Events(),
            getMenuEvents(),
            getL3Events()
        ))
    }

    private fun getStartEvent(): Observable<PadEvent> {
        return findViewById<SmallSingleButton>(R.id.start)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_START))
    }

    private fun getSelectEvent(): Observable<PadEvent> {
        return findViewById<SmallSingleButton>(R.id.select)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_SELECT))
    }

    private fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.actions)
            .getEvents()
            .compose(EventsTransformers.actionButtonsMap(
                    KeyEvent.KEYCODE_BUTTON_Y,
                    KeyEvent.KEYCODE_BUTTON_X,
                    KeyEvent.KEYCODE_BUTTON_B,
                    KeyEvent.KEYCODE_BUTTON_A)
            )
    }

    private fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.direction)
            .getEvents()
            .compose(EventsTransformers.directionPadMap())
    }

    private fun getL1Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.l1)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_L1))
    }

    private fun getR1Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.r1)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_R1))
    }

    private fun getMenuEvents(): Observable<PadEvent> {
        return findViewById<IconButton>(R.id.menu)
            .getEvents()
            .compose(EventsTransformers.clickMap(OptionType.SETTINGS))
    }

    private fun getL3Events(): Observable<PadEvent> {
        return findViewById<IconButton>(R.id.l3)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_THUMBL))
    }
}
