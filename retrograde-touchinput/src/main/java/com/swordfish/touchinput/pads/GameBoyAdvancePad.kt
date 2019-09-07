package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.data.EventsTransformers
import com.swordfish.touchinput.data.PadEvent
import com.swordfish.touchinput.views.ActionButtons
import com.swordfish.touchinput.views.DirectionPad
import com.swordfish.touchinput.views.LargeSingleButton
import com.swordfish.touchinput.views.SmallSingleButton
import io.reactivex.Observable

class GameBoyAdvancePad @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : GamePadView(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_gba, this)
    }

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(listOf(
            getStartEvent(),
            getSelectEvent(),
            getDirectionEvents(),
            getActionEvents(),
            getR1Events(),
            getL1Events()))
    }

    fun getStartEvent(): Observable<PadEvent> {
        return findViewById<SmallSingleButton>(R.id.gba_start)
                .getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_START))
    }

    fun getSelectEvent(): Observable<PadEvent> {
        return findViewById<SmallSingleButton>(R.id.gba_select)
                .getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_SELECT))
    }

    fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.gba_actions)
                .getEvents()
                .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_A))

    }

    fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.gba_direction).getEvents()
                .compose(EventsTransformers.directionPadMap())
    }

    fun getL1Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.gba_l1).getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_L1))
    }

    fun getR1Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.gba_r1).getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_R1))
    }
}
