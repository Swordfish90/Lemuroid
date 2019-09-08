package com.swordfish.touchinput.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.data.ButtonEvent
import com.swordfish.touchinput.interfaces.ButtonEventsSource
import io.reactivex.Observable
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class ActionButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ButtonEventsSource {

    private val events: PublishRelay<ButtonEvent> = PublishRelay.create()

    private var spacing: Float = 0.1f
    private var rows: Int = 2
    private var cols: Int = 2
    private var rotateButtons: Float = 0.0f
    private var supportsMultipleInputs: Boolean = false

    private var notRotatedWidth: Float = 0f
    private var notRotatedHeight: Float = 0f
    private var xPadding: Float = 0f
    private var yPadding: Float = 0f
    private var buttonSize: Float = 0f
    private var totalButtonSize: Float = 0f

    private val buttonsPressed = mutableSetOf<Int>()

    private var pressedDrawable: Drawable?
    private var normalDrawable: Drawable?

    private val touchRotationMatrix = Matrix()

    init {
        pressedDrawable = retrieveDrawable(R.drawable.action_pressed)
        normalDrawable = retrieveDrawable(R.drawable.action_normal)

        context.theme.obtainStyledAttributes(attrs, R.styleable.ActionButtons, defStyleAttr, 0)?.let {
            initializeFromAttributes(it)
        }
    }

    override fun getEvents(): Observable<ButtonEvent> = events

    private fun initializeFromAttributes(a: TypedArray) {
        supportsMultipleInputs = a.getBoolean(R.styleable.ActionButtons_multipleInputs, false)
        rows = a.getInt(R.styleable.ActionButtons_rows, 2)
        cols = a.getInt(R.styleable.ActionButtons_cols, 2)
        spacing = a.getFloat(R.styleable.ActionButtons_spacing, 0.1f)
        rotateButtons = a.getFloat(R.styleable.ActionButtons_rotateButtons, 45.0f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        buttonSize = resources.getDimension(R.dimen.size_action_button_item)
        totalButtonSize = buttonSize + buttonSize * spacing

        notRotatedWidth = totalButtonSize * cols
        notRotatedHeight = totalButtonSize * rows

        val radians = Math.toRadians(rotateButtons.toDouble())
        val rotatedWidth = (abs(notRotatedWidth * sin(radians)) + abs(notRotatedHeight * cos(radians))).toFloat()
        val rotatedHeight = (abs(notRotatedWidth * cos(radians)) + abs(notRotatedHeight * sin(radians))).toFloat()

        val width = getSize(MeasureSpec.getMode(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec), rotatedWidth.toInt())
        val height = getSize(MeasureSpec.getMode(heightMeasureSpec), MeasureSpec.getSize(heightMeasureSpec), rotatedHeight.toInt())

        xPadding = abs(width - notRotatedWidth) / 2f
        yPadding = abs(height - notRotatedHeight) / 2f

        setMeasuredDimension(width, height)
    }

    private fun getSize(widthMode: Int, widthSize: Int, expectedSize: Int): Int {
        return when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            else -> minOf(expectedSize, widthSize)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        touchRotationMatrix.reset()
        touchRotationMatrix.setRotate(-rotateButtons, width / 2f, height / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.rotate(rotateButtons, width / 2f, height / 2f)

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = toIndex(row, col)

                val drawable = if (index in buttonsPressed) { pressedDrawable } else { normalDrawable }

                drawable?.let {
                    val height = drawable.intrinsicHeight
                    val width = drawable.intrinsicWidth
                    val left = (xPadding + col * totalButtonSize).toInt()
                    val top = (yPadding + row * totalButtonSize).toInt()

                    drawable.setBounds(left, top, left + width, top + height)
                    drawable.draw(canvas)
                }
            }
        }

        canvas.restore()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                handleTouchEvent(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                allKeysReleased()
                invalidate()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun handleTouchEvent(originalX: Float, originalY: Float) {
        val point = floatArrayOf(originalX, originalY)

        touchRotationMatrix.mapPoints(point)
        val x = (point[0] - xPadding)
        val y = (point[1] - yPadding)

        val isXInRange = x in (0f..notRotatedWidth)
        val isYInRange = y in (0f..notRotatedHeight)

        if (isXInRange && isYInRange) {
            val col = floor(x / totalButtonSize).toInt()
            val row = floor(y / totalButtonSize).toInt()
            val index = toIndex(row, col)

            if (buttonsPressed.contains(index).not()) {
                if (supportsMultipleInputs.not()) {
                    allKeysReleased()
                }
                onKeyPressed(index)
            }
            postInvalidate()
        }
    }

    private fun toIndex(row: Int, col: Int) = row * cols + col

    private fun onKeyPressed(index: Int) {
        buttonsPressed.add(index)
        events.accept(ButtonEvent(KeyEvent.ACTION_DOWN, index))
    }

    private fun allKeysReleased() {
        buttonsPressed.map { events.accept(ButtonEvent(KeyEvent.ACTION_UP, it)) }
        buttonsPressed.clear()
    }

    private fun retrieveDrawable(drawableId: Int): Drawable? {
        return AppCompatResources.getDrawable(context, drawableId)
    }
}
