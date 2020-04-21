package com.swordfish.touchinput.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.ButtonEventsSource
import com.swordfish.touchinput.utils.TextPainter
import io.reactivex.Observable
import kotlin.math.min

class SingleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), ButtonEventsSource {

    private val events: PublishRelay<ViewEvent.Button> = PublishRelay.create()
    private val textPainter = TextPainter(context.resources)

    private var label: String = ""

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SingleButton, defStyleAttr, 0).let {
            initializeFromAttributes(it)
        }
        setOnTouchListener { _, event -> handleTouchEvent(event); true }
        setBackgroundResource(R.drawable.single_button_selector)
    }

    private fun initializeFromAttributes(a: TypedArray) {
        label = a.getString(R.styleable.SingleButton_label) ?: ""
        a.recycle()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (label.isNotBlank()) {
            textPainter.paintText(0f, 0f, width.toFloat(), height.toFloat(), label, canvas)
        }
    }

    fun getSuggestedButtonWidth(): Int {
        return R.dimen.size_button_small
    }

    fun getSuggestedButtonHeight(): Int {
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
