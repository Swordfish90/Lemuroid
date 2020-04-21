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
import com.swordfish.touchinput.views.SingleButton
import io.reactivex.Observable

class Atari2600Pad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseGamePad(
    context, attrs, defStyleAttr,
    SemiPadConfig(R.layout.layout_atari2600_left, 3, 5),
    SemiPadConfig(R.layout.layout_atari2600_right, 3, 5)
) {

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(listOf(
                getDirectionEvents(),
                getActionEvents(),
                getSelectEvents(),
                getStartEvents(),
                getL1Events(),
                getL2Events(),
                getMenuEvents()
        ))
    }

    private fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.actions)
                .getEvents()
                .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_BUTTON_B))
    }

    private fun getL1Events(): Observable<PadEvent> {
        return findViewById<SingleButton>(R.id.l1)
                .getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_L1))
    }

    private fun getL2Events(): Observable<PadEvent> {
        return findViewById<SingleButton>(R.id.l2)
                .getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_L2))
    }

    private fun getSelectEvents(): Observable<PadEvent> {
        return findViewById<SingleButton>(R.id.select)
                .getEvents()
                .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_BUTTON_SELECT))
    }

    private fun getStartEvents(): Observable<PadEvent> {
        return findViewById<SingleButton>(R.id.start)
                .getEvents()
                .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_BUTTON_START))
    }

    private fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.direction).getEvents()
                .compose(EventsTransformers.directionPadMap())
    }

    private fun getMenuEvents(): Observable<PadEvent> {
        return findViewById<IconButton>(R.id.menu)
                .getEvents()
                .compose(EventsTransformers.clickMap(OptionType.SETTINGS))
    }
}
