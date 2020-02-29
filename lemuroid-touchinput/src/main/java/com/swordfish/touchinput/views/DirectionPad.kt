package com.swordfish.touchinput.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.StickEventsSource
import io.reactivex.Observable
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

class DirectionPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), StickEventsSource {

    companion object {

        private const val DRAWABLE_SIZE_SCALING = 0.75
        private const val BUTTON_COUNT = 8
        private const val ROTATE_BUTTONS = 22.5f
        private const val SINGLE_BUTTON_ANGLE = 360f / BUTTON_COUNT

        const val BUTTON_RIGHT = 0
        const val BUTTON_DOWN_RIGHT = 1
        const val BUTTON_DOWN = 2
        const val BUTTON_DOWN_LEFT = 3
        const val BUTTON_LEFT = 4
        const val BUTTON_UP_LEFT = 5
        const val BUTTON_UP = 6
        const val BUTTON_UP_RIGHT = 7

        private val DRAWABLE_BUTTONS = setOf(BUTTON_RIGHT, BUTTON_DOWN, BUTTON_LEFT, BUTTON_UP)
    }

    private var deadZone: Float = 0f
    private var buttonCenterDistance: Float = 0f

    private val touchRotationMatrix = Matrix()
    private var radius: Int = 0

    private var normalDrawable: Drawable? = null
    private var pressedDrawable: Drawable? = null

    private val events: PublishRelay<ViewEvent.Stick> = PublishRelay.create()

    private var currentIndex: Int? = null

    private var drawableSize: Int = 0

    init {
        val padStyleable = R.styleable.DirectionPad
        val defaultStyle = R.style.default_directionpad
        context.theme.obtainStyledAttributes(attrs, padStyleable, defStyleAttr, defaultStyle).let {
            initializeFromAttributes(it)
        }
    }

    override fun getEvents(): Observable<ViewEvent.Stick> = events.distinctUntilChanged()

    private fun initializeFromAttributes(a: TypedArray) {
        deadZone = a.getFloat(R.styleable.DirectionPad_deadZone, 0f)
        buttonCenterDistance = a.getFloat(R.styleable.DirectionPad_buttonCenterDistance, 0f)
        normalDrawable = a.getDrawable(R.styleable.DirectionPad_rightArrowDrawable)
        pressedDrawable = a.getDrawable(R.styleable.DirectionPad_rightArrowPressedDrawable)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getSize(MeasureSpec.getMode(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec))
        val height = getSize(MeasureSpec.getMode(heightMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))

        val diameter = minOf(width, height)
        setMeasuredDimension(diameter, diameter)
        radius = diameter / 2

        drawableSize = (radius * DRAWABLE_SIZE_SCALING).roundToInt()
    }

    private fun getSize(widthMode: Int, widthSize: Int): Int {
        return when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            else -> minOf(resources.getDimension(R.dimen.default_dial_size).toInt(), widthSize)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        touchRotationMatrix.reset()
        touchRotationMatrix.setRotate(ROTATE_BUTTONS)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pressedButtons = convertDiagonals(currentIndex)

        for (i in 0..BUTTON_COUNT) {
            val cAngle = SINGLE_BUTTON_ANGLE * i

            val isPressed = i in pressedButtons

            getStateDrawable(i, isPressed)?.let {
                val height = drawableSize
                val width = drawableSize
                val angle = toRadians((cAngle - ROTATE_BUTTONS + SINGLE_BUTTON_ANGLE / 2f).toDouble())
                val left = (radius * buttonCenterDistance * cos(angle) + radius).toInt() - width / 2
                val top = (radius * buttonCenterDistance * sin(angle) + radius).toInt() - height / 2
                val xPivot = left + width / 2f
                val yPivot = top + height / 2f

                canvas.rotate(i * SINGLE_BUTTON_ANGLE, xPivot, yPivot)

                it.setBounds(left, top, left + width, top + height)
                it.draw(canvas)

                canvas.rotate(-i * SINGLE_BUTTON_ANGLE, xPivot, yPivot)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                handleTouchEvent(event.x - radius, event.y - radius)
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentIndex = null
                events.accept(ViewEvent.Stick(0.0f, 0.0f, false))
                postInvalidate()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun handleTouchEvent(originalX: Float, originalY: Float) {
        val point = floatArrayOf(originalX, originalY)

        if (isOutsideDeadzone(point[0], point[1])) {
            touchRotationMatrix.mapPoints(point)
            val x = point[0]
            val y = point[1]

            val angle = (atan2(y, x) * 180 / Math.PI + 360f) % 360f
            val index = floor(angle / SINGLE_BUTTON_ANGLE).toInt()

            if (index != currentIndex) {
                val haptic = currentIndex?.let { prevIndex -> (prevIndex % 2) == 0 } ?: true

                currentIndex = index
                events.accept(ViewEvent.Stick(
                        cos(index * toRadians(SINGLE_BUTTON_ANGLE.toDouble())).toFloat(),
                        sin(index * toRadians(SINGLE_BUTTON_ANGLE.toDouble())).toFloat(),
                        haptic
                ))
                postInvalidate()
            }
        }
    }

    private fun isOutsideDeadzone(x: Float, y: Float): Boolean {
        return x * x + y * y > radius * deadZone * radius * deadZone
    }

    private fun getStateDrawable(index: Int, isPressed: Boolean): Drawable? {
        return if (index in DRAWABLE_BUTTONS) {
            if (isPressed) { pressedDrawable } else { normalDrawable }
        } else {
            null
        }
    }

    private fun convertDiagonals(currentIndex: Int?): Set<Int> {
        return when (currentIndex) {
            BUTTON_DOWN_RIGHT -> setOf(BUTTON_DOWN, BUTTON_RIGHT)
            BUTTON_DOWN_LEFT -> setOf(BUTTON_DOWN, BUTTON_LEFT)
            BUTTON_UP_LEFT -> setOf(BUTTON_UP, BUTTON_LEFT)
            BUTTON_UP_RIGHT -> setOf(BUTTON_UP, BUTTON_RIGHT)
            null -> setOf()
            else -> setOf(currentIndex)
        }
    }
}
