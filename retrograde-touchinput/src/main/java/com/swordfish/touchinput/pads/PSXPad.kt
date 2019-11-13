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

class PSXPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseGamePad(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_psx, this)
    }

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(listOf(
            getStartEvent(),
            getSelectEvent(),
            getDirectionEvents(),
            getActionEvents(),
            getR1Events(),
            getR2Events(),
            getL1Events(),
            getL2Events()))
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
                    KeyEvent.KEYCODE_Y,
                    KeyEvent.KEYCODE_X,
                    KeyEvent.KEYCODE_B,
                    KeyEvent.KEYCODE_A)
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

    private fun getL2Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.l2)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_L2))
    }

    private fun getR1Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.r1)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_R1))
    }

    private fun getR2Events(): Observable<PadEvent> {
        return findViewById<LargeSingleButton>(R.id.r2)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_R2))
    }
}
