package com.swordfish.touchinput.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.common.math.linearInterpolation
import io.reactivex.Observable
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.sign

class TiltSensor(context: Context): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val primaryDisplay = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

    private val restOrientationsBuffer = mutableListOf<FloatArray>()
    private var restOrientation: FloatArray? = null

    private val tiltEvents = PublishRelay.create<FloatArray>()

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var maxRotation: Float = MAX_MAX_ROTATION
    private var deadZone: Float = 0.1f * maxRotation

    init {
        setSensitivity(0.5f)
    }

    fun getTiltEvents(): Observable<FloatArray> = tiltEvents

    fun enable() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.also { magneticField ->
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun disable() {
        sensorManager.unregisterListener(this)
        restOrientation = null
        restOrientationsBuffer.clear()
    }

    fun setSensitivity(sensitivity: Float) {
        maxRotation = linearInterpolation(sensitivity, MAX_MAX_ROTATION, MIN_MAX_ROTATION)
        deadZone = maxRotation * 0.1f
        Timber.d("Setting tilt sensitivity max angle: ${Math.toDegrees(maxRotation.toDouble())}")
    }

    // TODO FILIPPO... We should handle this case
    fun isAvailable(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing here
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            onNewRotationVector(event.values)
        }
    }

    private fun onNewRotationVector(rotationVector: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        val (xAxis, yAxis) = getAxisRemapForDisplayRotation()

        SensorManager.remapCoordinateSystem(rotationMatrix, xAxis, yAxis, remappedRotationMatrix)
        SensorManager.getOrientation(remappedRotationMatrix, orientationAngles)

        val xRotation = chooseBestAngleRepresentation(orientationAngles[1], Math.PI.toFloat())
        val yRotation = chooseBestAngleRepresentation(orientationAngles[2], Math.PI.toFloat())

        if (restOrientation == null && restOrientationsBuffer.size < MEASUREMENTS_BUFFER_SIZE) {
            restOrientationsBuffer.add(floatArrayOf(yRotation, xRotation))
        } else if (restOrientation == null && restOrientationsBuffer.size >= MEASUREMENTS_BUFFER_SIZE) {
            restOrientation = floatArrayOf(
                restOrientationsBuffer.map { it[0] }.sum() / restOrientationsBuffer.size,
                restOrientationsBuffer.map { it[1] }.sum() / restOrientationsBuffer.size
            )
        } else {
            val x = clamp(applyDeadZone(yRotation - restOrientation!![0], deadZone) / (maxRotation))
            val y = clamp(-applyDeadZone(xRotation - restOrientation!![1], deadZone) / (maxRotation))
            tiltEvents.accept(floatArrayOf(x, y))
        }
    }

    private fun getAxisRemapForDisplayRotation(): Pair<Int, Int> {
        return when (primaryDisplay.rotation) {
            Surface.ROTATION_0 -> SensorManager.AXIS_X to SensorManager.AXIS_Y
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
    }

    private fun chooseBestAngleRepresentation(x: Float, offset: Float): Float {
        return sequenceOf(x, x + offset, x - offset).minBy { abs(it) }!!
    }

    private fun applyDeadZone(x: Float, deadzone: Float): Float {
        return if (abs(x) < deadzone) { 0f } else x - sign(x) * deadzone
    }

    private fun clamp(x: Float): Float {
        return maxOf(minOf(x, 1f), -1f)
    }

    companion object {
        const val MEASUREMENTS_BUFFER_SIZE = 5
        val MAX_MAX_ROTATION = Math.toRadians(20.0).toFloat()
        val MIN_MAX_ROTATION = Math.toRadians(2.5).toFloat()
    }
}
