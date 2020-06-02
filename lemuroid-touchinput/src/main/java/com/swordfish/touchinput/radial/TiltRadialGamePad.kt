package com.swordfish.touchinput.radial

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.sensors.TiltSensor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class TiltRadialGamePad @JvmOverloads constructor(
    leftConfig: RadialGamePadConfig,
    rightConfig: RadialGamePadConfig,
    context: Context,
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

        leftPad = RadialGamePad(leftConfig, context)
        leftContainer.addView(leftPad)

        rightPad = RadialGamePad(rightConfig, context)
        rightContainer.addView(rightPad)
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
                    if (it.type == GestureType.DOUBLE_TAP && it.id in tiltTrackedIds) {
                        startTrackingId(it.id)
                    } else if (it.id == currentTiltId) {
                        stopTrackingId(it.id)
                    }
                }
            }
        }
    }

    fun setTiltSensitivity(tiltSensitivity: Float) {
        tiltSensor.setSensitivity(tiltSensitivity)
    }

    private fun stopTrackingId(id: Int) {
        currentTiltId = null
        tiltSensor.shouldRun = false
        leftPad.clearMotionEvent(id)
        rightPad.clearMotionEvent(id)
    }

    private fun startTrackingId(id: Int) {
        if (currentTiltId != id) {
            currentTiltId?.let { stopTrackingId(it) }
            currentTiltId = id
            tiltSensor.shouldRun = true
        }
    }
}
