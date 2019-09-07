package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.data.EventsTransformers
import com.swordfish.touchinput.data.PadEvent
import com.swordfish.touchinput.views.ActionButtons
import com.swordfish.touchinput.views.DirectionPad
import com.swordfish.touchinput.views.base.BaseSingleButton
import io.reactivex.Observable

class GenesisPad @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : GamePadView(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_genesis, this)
    }

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(
                getStartEvent(),
                getDirectionEvents(),
                getActionEvents()
        )
    }

    fun getStartEvent(): Observable<PadEvent> {
        return findViewById<BaseSingleButton>(R.id.genesis_start)
                .getEvents()
                .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_START))
    }

    fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.genesis_actions)
                .getEvents()
                .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_C))

    }

    fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.genesis_direction).getEvents()
                .compose(EventsTransformers.directionPadMap())
    }
}
