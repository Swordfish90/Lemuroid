package com.swordfish.touchinput.radial.sensors

import android.view.KeyEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import gg.jam.jampadcompose.ids.ContinuousDirectionId
import gg.jam.jampadcompose.ids.ControlId
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.inputstate.InputState
import java.io.Serializable

val TILT_CONFIGURATION_DISABLED = TiltConfiguration.Disabled
val TILT_CONFIGURATION_CROSS = TiltConfiguration.Cross(0)
val TILT_CONFIGURATION_ANALOG_LEFT = TiltConfiguration.Analog(1)
val TILT_CONFIGURATION_ANALOG_RIGHT = TiltConfiguration.Analog(2)
val TILT_CONFIGURATION_L_R = TiltConfiguration.ButtonPair(KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1)
val TILT_CONFIGURATION_L1_R1 = TiltConfiguration.ButtonPair(KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1)
val TILT_CONFIGURATION_L2_R2 = TiltConfiguration.ButtonPair(KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R2)

sealed interface TiltConfiguration : Serializable {
    fun process(values: FloatArray): InputState
    fun controlIds(): Set<ControlId>

    data object Disabled : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            return InputState()
        }

        override fun controlIds(): Set<ControlId> {
            return emptySet()
        }
    }

    data class Cross(val directionId: Int) : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            val offset = Offset(values[0], -values[1]).round()
            return InputState().setDiscreteDirection(DiscreteDirectionId(directionId), offset.toOffset())
        }

        override fun controlIds(): Set<ControlId> {
            return setOf(DiscreteDirectionId(directionId))
        }
    }

    data class Analog(val directionId: Int) : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            val offset = Offset(values[0], -values[1])
            return InputState().setContinuousDirection(ContinuousDirectionId(directionId), offset)
        }

        override fun controlIds(): Set<ControlId> {
            return setOf(ContinuousDirectionId(directionId))
        }
    }

    data class ButtonPair(val leftButtonId: Int, val rightButtonId: Int) : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            val xTilt = values[0]
            val yTilt = values[1]

            val leftPressed = xTilt < -0.75f
            val rightPressed = xTilt > 0.75f
            val bothPressed = !leftPressed && !rightPressed && yTilt < -0.9f

            return InputState()
                .setDigitalKey(KeyId(leftButtonId), leftPressed || bothPressed)
                .setDigitalKey(KeyId(rightButtonId), rightPressed || bothPressed)
        }

        override fun controlIds(): Set<ControlId> {
            return setOf(KeyId(leftButtonId), KeyId(rightButtonId))
        }
    }
}
