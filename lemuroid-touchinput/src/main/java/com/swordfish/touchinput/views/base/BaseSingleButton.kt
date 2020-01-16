package com.swordfish.touchinput.views.base

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.ButtonEventsSource
import io.reactivex.Observable
import kotlin.math.min

abstract class BaseSingleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr), ButtonEventsSource {

    private val events: PublishRelay<ViewEvent.Button> = PublishRelay.create()

    init {
        setOnTouchListener { _, event -> handleTouchEvent(event); true }
    }

    private fun handleTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                events.accept(ViewEvent.Button(KeyEvent.ACTION_DOWN, 0, true))
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                events.accept(ViewEvent.Button(KeyEvent.ACTION_UP, 0, false))
            }
        }
    }

    override fun getEvents(): Observable<ViewEvent.Button> = events

    open fun getSuggestedButtonWidth(): Int {
        return R.dimen.size_button_small
    }

    open fun getSuggestedButtonHeight(): Int {
        return R.dimen.size_button_small
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = resources.getDimension(getSuggestedButtonWidth()).toInt()
        val desiredHeight = resources.getDimension(getSuggestedButtonHeight()).toInt()
        setMeasuredDimension(
            measureDimension(desiredWidth, widthMeasureSpec),
            measureDimension(desiredHeight, heightMeasureSpec)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }
}
