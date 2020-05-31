package com.swordfish.touchinput.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.PadBusEvent
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.PadBusSource
import com.swordfish.touchinput.interfaces.StickEventsSource
import com.swordfish.touchinput.radial.sensors.TiltSensor
import com.swordfish.touchinput.utils.DoubleTapListener
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
) : FrameLayout(context, attrs, defStyleAttr), StickEventsSource, PadBusSource {

    private val events: PublishRelay<ViewEvent.Stick> = PublishRelay.create()
    private val padBusEvents: PublishRelay<PadBusEvent> = PublishRelay.create()

    private val tiltSensor = TiltSensor(context)

    private val stickView: JoystickView
    private val tiltView: TiltSensorView

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, DoubleTapListener {
        toggleTiltMode()
    })

    init {
        LayoutInflater.from(context).inflate(R.layout.base_layout_stick, this)

        stickView = findViewById(R.id.joystick)
        tiltView = findViewById(R.id.tilt_view)

        stickView.setOnMoveListener(this::handleMoveEvent, 16)
    }

    override fun onBusEvent(event: PadBusEvent) {
        Timber.d("Received bus event: $event")
        when (event) {
            is PadBusEvent.OnResume -> tiltSensor.isAllowedToRun = true
            is PadBusEvent.OnPause -> tiltSensor.isAllowedToRun = false
            is PadBusEvent.TiltEnabled -> disableTiltMode()
            is PadBusEvent.SetTiltSensitivity -> tiltSensor.setSensitivity(event.tiltSensitivity)
        }
    }

    private fun toggleTiltMode() {
        if (!tiltSensor.shouldRun) {
            enableTiltMode()
        } else {
            disableTiltMode()
        }
    }

    private fun disableTiltMode() {
        stickView.visibility = View.VISIBLE
        tiltView.visibility = View.INVISIBLE
        stickView.setOnMoveListener(this::handleMoveEvent, 16)
        tiltSensor.shouldRun = false
        padBusEvents.accept(PadBusEvent.TiltDisabled(id))
    }

    private fun enableTiltMode() {
        if (!tiltSensor.isAvailable()) { return }
        stickView.visibility = View.INVISIBLE
        tiltView.visibility = View.VISIBLE
        stickView.setOnMoveListener(null, 16)
        tiltSensor.shouldRun = true
        padBusEvents.accept(PadBusEvent.TiltEnabled(id))
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // We send a dummy haptic event. This should performs vibration when the stick is pressed the first time.
        if (event.action == MotionEvent.ACTION_DOWN) {
            events.accept(ViewEvent.Stick(0.0f, 0.0f, true))
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN && tiltSensor.shouldRun) {
            disableTiltMode()
            return true
        }

        return when {
            gestureDetector.onTouchEvent(event) -> true
            else -> super.onInterceptTouchEvent(event)
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

    override fun getBusEvents(): Observable<PadBusEvent> = padBusEvents

    override fun getView() = this

    override fun getEvents(): Observable<ViewEvent.Stick> = events.mergeWith(getTiltEvents())

    private fun getTiltEvents() = tiltSensor.getTiltEvents()
        .doOnNext { tiltView.updatePosition(it[0], it[1]) }
        .map { ViewEvent.Stick(it[0], it[1], false) }
}
