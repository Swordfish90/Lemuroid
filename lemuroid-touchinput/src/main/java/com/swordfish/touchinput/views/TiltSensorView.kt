package com.swordfish.touchinput.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.swordfish.touchinput.controller.R
import kotlin.math.sqrt

class TiltSensorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var foregroundRatio = 0.25f
    private var backgroundRatio = 0.75f
    private var size = 0
    private var outerRadius = 0f
    private var innerRadius = 0f
    private var centerPositionX = 0f
    private var centerPositionY = 0f

    private var currentX: Float = 0f
    private var currentY: Float = 0f

    private val backgroundPaint: Paint = Paint()
    private val foregroundPaint: Paint = Paint()

    init {
        backgroundPaint.isAntiAlias = true
        backgroundPaint.color = Color.BLACK
        backgroundPaint.style = Paint.Style.FILL

        foregroundPaint.isAntiAlias = true
        foregroundPaint.color = Color.WHITE
        foregroundPaint.style = Paint.Style.FILL

        context.theme.obtainStyledAttributes(attrs, R.styleable.TiltSensorView, defStyleAttr, 0).let {
            initializeFromAttributes(it)
        }
    }

    private fun initializeFromAttributes(attributes: TypedArray) {
        foregroundRatio = attributes.getFraction(R.styleable.TiltSensorView_foregroundSizeRatio, 1, 1, 0.25f)
        backgroundRatio = attributes.getFraction(R.styleable.TiltSensorView_backgroundSizeRatio, 1, 1, 0.75f)
        backgroundPaint.color = attributes.getColor(R.styleable.TiltSensorView_backgroundColor, Color.BLACK)
        foregroundPaint.color = attributes.getColor(R.styleable.TiltSensorView_foregroundColor, Color.WHITE)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = true

    fun updatePosition(x: Float, y: Float) {
        currentX = x * sqrt(1 - (y * y / 2))
        currentY = y * sqrt(1 - (x * x / 2))
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerPositionX, centerPositionY, outerRadius, backgroundPaint)
        canvas.drawCircle(
            centerPositionX + currentX * (size - 2 * innerRadius) / 2f,
            centerPositionY + currentY * (size - 2 * innerRadius) / 2f,
            innerRadius,
            foregroundPaint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerPositionX = w / 2f
        centerPositionY = h / 2f
        size = minOf(w, h)
        outerRadius = (size / 2f) * backgroundRatio
        innerRadius = (size / 2f) * foregroundRatio
    }
}
