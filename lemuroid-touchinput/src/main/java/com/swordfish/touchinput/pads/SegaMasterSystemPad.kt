package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.EventsTransformers
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.views.ActionButtons
import com.swordfish.touchinput.views.DirectionPad
import io.reactivex.Observable

class SegaMasterSystemPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseGamePad(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_sms, this)
    }

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(
                getDirectionEvents(),
                getActionEvents()
        )
    }

    private fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.actions)
                .getEvents()
                .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_A))
    }

    private fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.direction).getEvents()
                .compose(EventsTransformers.directionPadMap())
    }
}
