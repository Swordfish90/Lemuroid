package com.swordfish.touchinput.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.StickEventsSource
import com.swordfish.touchinput.sensors.TiltSensor
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.reactivex.Observable
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Stick @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : JoystickView(context, attrs, defStyleAttr), StickEventsSource, LifecycleObserver {

    private val events: PublishRelay<ViewEvent.Stick> = PublishRelay.create()
    private val tiltSensor = TiltSensor(context)

    private var allowTilt: Boolean = false
    private var useTilt: Boolean = false

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            toggleSensors()
            return true
        }
    })

    init {
        setOnMoveListener(this::handleMoveEvent, 16)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        allowTilt = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        allowTilt = false
        disableTiltMode()
    }

    private fun toggleSensors() {
        Toast.makeText(context, "Toggled tilt mode", Toast.LENGTH_SHORT).show()
        if (!useTilt && allowTilt) {
            enableTiltMode()
        } else {
            disableTiltMode()
        }
    }

    private fun disableTiltMode() {
        useTilt = false
        setOnMoveListener(this::handleMoveEvent, 16)
        tiltSensor.disable()
    }

    private fun enableTiltMode() {
        useTilt = true
        setOnMoveListener(null, 16)
        tiltSensor.enable()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // We send a dummy haptic event. This should performs vibration when the stick is pressed the first time.
        if (event.action == MotionEvent.ACTION_DOWN) {
            events.accept(ViewEvent.Stick(0.0f, 0.0f, true))
        }

        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun handleMoveEvent(angle: Int, strength: Int) {
        val u = strength / 100f * cos(Math.toRadians(-angle.toDouble()))
        val v = strength / 100f * sin(Math.toRadians(-angle.toDouble()))
        val (x, y) = mapEllipticalDiskCoordinatesToSquare(u, v)
        events.accept(ViewEvent.Stick(x.toFloat(), y.toFloat(), false))
    }

    private fun mapEllipticalDiskCoordinatesToSquare(u: Double, v: Double): Pair<Double, Double> {
        val u2 = u * u
        val v2 = v * v
        val twoSqrt2 = 2.0 * sqrt(2.0)
        val subTermX = 2.0 + u2 - v2
        val subTermY = 2.0 - u2 + v2
        val termX1 = subTermX + u * twoSqrt2
        val termX2 = subTermX - u * twoSqrt2
        val termY1 = subTermY + v * twoSqrt2
        val termY2 = subTermY - v * twoSqrt2

        val x = (0.5 * sqrt(termX1) - 0.5 * sqrt(termX2))
        val y = (0.5 * sqrt(termY1) - 0.5 * sqrt(termY2))

        return x to y
    }

    override fun getEvents(): Observable<ViewEvent.Stick> {
        val tiltEvents = tiltSensor.getTiltEvents()
                .map { ViewEvent.Stick(it[0], it[1], false) }
                .doOnNext { Timber.i("Filippo $it") }
        return events.mergeWith(tiltEvents)
    }
}
