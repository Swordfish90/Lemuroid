package com.swordfish.touchinput.radial.sensors

import android.view.KeyEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import gg.padkit.ids.Id
import gg.padkit.inputstate.InputState
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

    fun controlIds(): Set<Id>

    data object Disabled : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            return InputState()
        }

        override fun controlIds(): Set<Id> {
            return emptySet()
        }
    }

    data class Cross(val directionId: Int) : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            val offset = Offset(values[0], -values[1]).round()
            return InputState().setDiscreteDirection(Id.DiscreteDirection(directionId), offset.toOffset())
        }

        override fun controlIds(): Set<Id> {
            return setOf(Id.DiscreteDirection(directionId))
        }
    }

    data class Analog(val directionId: Int) : TiltConfiguration {
        override fun process(values: FloatArray): InputState {
            val offset = Offset(values[0], -values[1])
            return InputState().setContinuousDirection(Id.ContinuousDirection(directionId), offset)
        }

        override fun controlIds(): Set<Id> {
            return setOf(Id.ContinuousDirection(directionId))
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
                .setDigitalKey(Id.Key(leftButtonId), leftPressed || bothPressed)
                .setDigitalKey(Id.Key(rightButtonId), rightPressed || bothPressed)
        }

        override fun controlIds(): Set<Id> {
            return setOf(Id.Key(leftButtonId), Id.Key(rightButtonId))
        }
    }
}
