package com.swordfish.touchinput.views.base

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.data.ButtonEvent
import com.swordfish.touchinput.interfaces.ButtonEventsSource
import io.reactivex.Observable

abstract class BaseSingleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr), ButtonEventsSource {

    private val events: PublishRelay<ButtonEvent> = PublishRelay.create()

    init {
        setOnTouchListener { _, event -> handleTouchEvent(event); true }
    }

    private fun handleTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                events.accept(ButtonEvent(KeyEvent.ACTION_DOWN, 0))
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                events.accept(ButtonEvent(KeyEvent.ACTION_UP, 0))
            }
        }
    }

    override fun getEvents(): Observable<ButtonEvent> = events
}
