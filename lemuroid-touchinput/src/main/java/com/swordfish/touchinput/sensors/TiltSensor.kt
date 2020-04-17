package com.swordfish.touchinput.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlin.math.abs
import kotlin.math.sign

class TiltSensor(context: Context): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val primaryDisplay = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

    private var magnetometerReading: FloatArray = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val outRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val firstReadingsBuffer = mutableListOf<FloatArray>()
    private var firstReading: FloatArray? = null

    private val tiltEvents = PublishRelay.create<FloatArray>()

    fun enable() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.also { magneticField ->
            sensorManager.registerListener(
                    this,
                    magneticField,
                    SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun disable() {
        sensorManager.unregisterListener(this)
        firstReading = null
        firstReadingsBuffer.clear()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing here
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        updateOrientationAngles()
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, magnetometerReading)

        var xAxis: Int = SensorManager.AXIS_X
        var yAxis: Int = SensorManager.AXIS_Y

        when(primaryDisplay.rotation) {
            Surface.ROTATION_0 -> {
                xAxis = SensorManager.AXIS_X
                yAxis = SensorManager.AXIS_Y
            }
            Surface.ROTATION_90 -> {
                xAxis = SensorManager.AXIS_Y
                yAxis = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_270 -> {
                xAxis = SensorManager.AXIS_MINUS_Y
                yAxis = SensorManager.AXIS_X
            }
            Surface.ROTATION_180 -> {
                xAxis = SensorManager.AXIS_MINUS_X
                yAxis = SensorManager.AXIS_MINUS_Y
            }
        }

        SensorManager.remapCoordinateSystem(rotationMatrix, xAxis, yAxis, outRotationMatrix)
        SensorManager.getOrientation(outRotationMatrix, orientationAngles)

        val xRotation = orientationAngles[1]
        val yRotation = orientationAngles[2]

        if (firstReading == null && firstReadingsBuffer.size < 5) {
            firstReadingsBuffer.add(floatArrayOf(yRotation, xRotation))
        } else if (firstReading == null && firstReadingsBuffer.size >= 5) {
            firstReading = floatArrayOf(
                firstReadingsBuffer.map { it[0] }.sum() / firstReadingsBuffer.size,
                firstReadingsBuffer.map { it[1] }.sum() / firstReadingsBuffer.size
            )
        } else {
            val x = clamp(applyDeadZone(yRotation - firstReading!![0], DEAD_ZONE) / (MAX_ROTATION))
            val y = clamp(-applyDeadZone(xRotation - firstReading!![1], DEAD_ZONE) / (MAX_ROTATION))
            tiltEvents.accept(floatArrayOf(x, y))
        }
    }

    private fun applyDeadZone(x: Float, deadzone: Float): Float {
        return if (abs(x) < deadzone) { 0f } else x - sign(x) * deadzone
    }

    private fun clamp(x: Float): Float {
        return maxOf(minOf(x, 1f), -1f)
    }

    fun getTiltEvents(): Observable<FloatArray> = tiltEvents

    companion object {
        val MAX_ROTATION = Math.toRadians(10.0).toFloat()
        val DEAD_ZONE = MAX_ROTATION * 0.1f
    }
}
