package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.EventsTransformers
import com.swordfish.touchinput.events.OptionType
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.interfaces.StickEventsSource
import com.swordfish.touchinput.views.ActionButtons
import com.swordfish.touchinput.views.DirectionPad
import com.swordfish.touchinput.views.IconButton
import com.swordfish.touchinput.views.SingleButton
import io.reactivex.Observable

class ArcadePad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseGamePad(
    context, attrs, defStyleAttr,
    SemipadConfig(R.layout.layout_arcade_left, 3, 6),
    SemipadConfig(R.layout.layout_arcade_right, 4, 6)
) {

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(listOf(
            getStartEvent(),
            getSelectEvent(),
            getDirectionEvents(),
            getActionEvents(),
            getMenuEvents()
        ))
    }

    private fun getStartEvent(): Observable<PadEvent> {
        return findViewById<SingleButton>(R.id.start)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_START))
    }

    private fun getSelectEvent(): Observable<PadEvent> {
        return findViewById<SingleButton>(R.id.select)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_SELECT))
    }

    private fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.actions)
            .getEvents()
            .compose(EventsTransformers.actionButtonsMap(
                    KeyEvent.KEYCODE_BUTTON_Y,
                    KeyEvent.KEYCODE_BUTTON_X,
                    KeyEvent.KEYCODE_BUTTON_L1,
                    KeyEvent.KEYCODE_BUTTON_B,
                    KeyEvent.KEYCODE_BUTTON_A,
                    KeyEvent.KEYCODE_BUTTON_R1)
            )
    }

    private fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.direction)
            .getEvents()
            .concatMap { Observable.just(
                PadEvent.Stick(StickEventsSource.SOURCE_DPAD, it.xAxis, it.yAxis, it.haptic),
                PadEvent.Stick(StickEventsSource.SOURCE_LEFT_STICK, it.xAxis, it.yAxis, it.haptic)
            ) }
    }

    private fun getMenuEvents(): Observable<PadEvent> {
        return findViewById<IconButton>(R.id.menu)
            .getEvents()
            .compose(EventsTransformers.clickMap(OptionType.SETTINGS))
    }
}
