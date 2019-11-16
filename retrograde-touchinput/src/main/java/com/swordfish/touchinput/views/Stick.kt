package com.swordfish.touchinput.views

import android.content.Context
import android.util.AttributeSet
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.StickEventsSource
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.reactivex.Observable
import kotlin.math.cos
import kotlin.math.sin

class Stick @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : JoystickView(context, attrs, defStyleAttr), StickEventsSource {

    private val events: PublishRelay<ViewEvent.Stick> = PublishRelay.create()

    init {
        setOnMoveListener(this::handleMoveEvent, 16)
    }

    private fun handleMoveEvent(angle: Int, strength: Int) {
        events.accept(ViewEvent.Stick(
            strength / 100f * cos(Math.toRadians(-angle.toDouble())).toFloat(),
            strength / 100f * sin(Math.toRadians(-angle.toDouble())).toFloat()
        ))
    }

    override fun getEvents(): Observable<ViewEvent.Stick> = events
}
