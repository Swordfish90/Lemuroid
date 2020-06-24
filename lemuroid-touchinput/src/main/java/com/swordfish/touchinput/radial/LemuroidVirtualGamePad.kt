package com.swordfish.touchinput.radial

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.swordfish.lemuroid.common.getAccentColor
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.math.linearInterpolation
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.sensors.TiltSensor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class LemuroidVirtualGamePad @JvmOverloads constructor(
    leftConfig: RadialGamePadConfig,
    rightConfig: RadialGamePadConfig,
    context: Context,
    private val baseScaling: Float = 1.0f,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleObserver {

    private var currentTiltId: Int? = null

    private val compositeDisposable = CompositeDisposable()

    private val tiltSensor: TiltSensor = TiltSensor(context)
    private val leftPad: RadialGamePad
    private val rightPad: RadialGamePad

    private val tiltTrackedIds = setOf(
        GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
        GLRetroView.MOTION_SOURCE_ANALOG_RIGHT
    )

    init {
        inflate(context, R.layout.base_gamepad, this)

        val leftContainer = findViewById<FrameLayout>(R.id.leftcontainer)
        val rightContainer = findViewById<FrameLayout>(R.id.rightcontainer)

        val padTheme = getGamePadTheme(context)

        leftPad = RadialGamePad(leftConfig.copy(theme = padTheme), DEFAULT_MARGINS_DP, context)
        leftContainer.addView(leftPad)

        rightPad = RadialGamePad(rightConfig.copy(theme = padTheme), DEFAULT_MARGINS_DP, context)
        rightContainer.addView(rightPad)
    }

    private fun getGamePadTheme(context: Context): RadialGamePadTheme {
        val accentColor = GraphicsUtils.colorToRgb(context.getAccentColor())
        val alpha = (255 * PRESSED_COLOR_ALPHA).roundToInt()
        val pressedColor = GraphicsUtils.rgbaToColor(accentColor + listOf(alpha))
        return RadialGamePadTheme(
            normalColor = context.getColor(R.color.touch_control_normal),
            pressedColor = pressedColor,
            primaryDialBackground = context.getColor(R.color.touch_control_background),
            textColor = context.getColor(R.color.touch_control_text)
        )
    }

    companion object {
        const val DEFAULT_MARGINS_DP = 8f

        const val DEFAULT_SCALE = 0.5f
        const val DEFAULT_PAD_ROTATION = 0.0f
        const val DEFAULT_OFFSET_Y = 0.0f
        const val DEFAULT_OFFSET_X = 0.0f

        const val MAX_ROTATION = 45f
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 1.5f

        const val PRESSED_COLOR_ALPHA = 0.5f
    }

    var orientation: Int by Delegates.observable(Configuration.ORIENTATION_PORTRAIT) { _, _, _ -> updateLayout() }

    var padScale: Float by Delegates.observable(DEFAULT_SCALE) { _, _, _ -> updateLayout() }
    var padRotation: Float by Delegates.observable(DEFAULT_PAD_ROTATION) { _, _, _ -> updateLayout() }
    var padOffsetY: Float by Delegates.observable(DEFAULT_OFFSET_Y) { _, _, _ -> updateLayout() }

    var tiltSensitivity: Float by Delegates.observable(0.6f) { _, _, value ->
        tiltSensor.setSensitivity(value)
    }

    private fun updateLayout() {
        val layout = findViewById<ConstraintLayout>(R.id.gamepadcontainer)

        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)

        val currentScale = linearInterpolation(padScale, MIN_SCALE, MAX_SCALE) * baseScaling
        val maxWidth = resources.getDimensionPixelSize(R.dimen.gamepad_max_width)
        constraintSet.constrainMaxWidth(R.id.leftcontainer, (maxWidth * currentScale).roundToInt())
        constraintSet.constrainMaxWidth(R.id.rightcontainer, (maxWidth * currentScale).roundToInt())

        val constrainHeight = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ConstraintSet.WRAP_CONTENT
        } else {
            ConstraintSet.MATCH_CONSTRAINT
        }

        constraintSet.constrainHeight(R.id.leftcontainer, constrainHeight)
        constraintSet.constrainHeight(R.id.rightcontainer, constrainHeight)

        constraintSet.applyTo(layout)

        leftPad.offsetX = linearInterpolation(DEFAULT_OFFSET_X, -1f, 1f)
        rightPad.offsetX = -linearInterpolation(DEFAULT_OFFSET_X, -1f, 1f)

        leftPad.offsetY = linearInterpolation(padOffsetY, 1f, -1f)
        rightPad.offsetY = linearInterpolation(padOffsetY, 1f, -1f)

        leftPad.secondaryDialRotation = linearInterpolation(padRotation, 0f, MAX_ROTATION)
        rightPad.secondaryDialRotation = -linearInterpolation(padRotation, 0f, MAX_ROTATION)

        requestLayout()
        invalidate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        tiltSensor.isAllowedToRun = false
        compositeDisposable.clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        compositeDisposable += tiltSensor
            .getTiltEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { sendTiltEvent(it) }

        tiltSensor.isAllowedToRun = true
    }

    private fun sendTiltEvent(sensorValues: FloatArray) {
        currentTiltId?.let {
            val xTilt = (sensorValues[0] + 1f) / 2f
            val yTilt = (sensorValues[1] + 1f) / 2f
            rightPad.simulateMotionEvent(it, xTilt, yTilt)
            leftPad.simulateMotionEvent(it, xTilt, yTilt)
        }
    }

    fun getEvents(): Observable<Event> {
        return Observable.merge(leftPad.events(), rightPad.events()).doOnNext {
            when (it) {
                is Event.Gesture -> {
                    if (it.type == GestureType.TRIPLE_TAP && it.id in tiltTrackedIds) {
                        startTrackingId(it.id)
                    } else if (it.id == currentTiltId) {
                        stopTrackingId(it.id)
                    }
                }
            }
        }
    }

    private fun stopTrackingId(id: Int) {
        currentTiltId = null
        tiltSensor.shouldRun = false
        leftPad.simulateClearMotionEvent(id)
        rightPad.simulateClearMotionEvent(id)
    }

    private fun startTrackingId(id: Int) {
        if (currentTiltId != id) {
            currentTiltId?.let { stopTrackingId(it) }
            currentTiltId = id
            tiltSensor.shouldRun = true
        }
    }
}
