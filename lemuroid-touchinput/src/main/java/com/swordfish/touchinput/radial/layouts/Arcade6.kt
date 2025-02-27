package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.swordfish.lemuroid.common.graphics.GraphicsUtils.rotatePoint
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonCoin
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.LemuroidButton
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.anchors.Anchor
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.utils.GeometryUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@Composable
fun JamPadScope.Arcade6Left(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD_AND_LEFT_STICK)) },
        secondaryDials = {
            SecondaryButtonCoin()
            SecondaryButtonStart(position = 1)
            SecondaryButtonMenuPlaceholder(settings)
        }
    )
}

@Composable
fun JamPadScope.Arcade6Right(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    val centralAnchors = rememberCentralAnchorsForSixButtons(settings.rotation)

    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            LemuroidControlFaceButtons(
                mainAnchors = centralAnchors,
                background = { },
                idsForegrounds = buildMap {
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_X)) {
                        LemuroidButton(pressed = it)
                    }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_A)) {
                        LemuroidButton(pressed = it)
                    }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_B)) {
                        LemuroidButton(pressed = it)
                    }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_Y)) {
                        LemuroidButton(pressed = it)
                    }
                },
            )
        },
        secondaryDials = {
            LemuroidControlButton(
                modifier = Modifier.radialPosition(90f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_L1),
            )
            LemuroidControlButton(
                modifier = Modifier.radialPosition(60f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_R1),
            )
            SecondaryButtonMenu(settings)
        }
    )
}

// TODO PADS... Move this somewhere shared...
@Composable
private fun rememberCentralAnchorsForSixButtons(rotation: Float): List<Anchor> {
    return remember(rotation) {
        val buttonSize = GeometryUtils.computeSizeOfItemsAroundCircumference(12)
        val d = 3f * buttonSize

        val rotationAngle = rotation * TouchControllerSettingsManager.MAX_ROTATION.toDouble()

        val delta = Offset(-tan(Math.toRadians(15.0)).toFloat(), 1f) * d * 1.25f
        val topLeftLine = Offset(0f, -1f - d)
        val topRightLine = Offset(sin(Math.toRadians(30.0)).toFloat(), -cos(Math.toRadians(30.0)).toFloat()) * (1f + d)

        val pointA = rotatePoint(topLeftLine + delta * 1.0f, rotationAngle)
        val pointB = rotatePoint(topLeftLine + delta * 2.0f, rotationAngle)
        val pointC = rotatePoint(topRightLine + delta * 1.0f, rotationAngle)
        val pointD = rotatePoint(topRightLine + delta * 2.0f, rotationAngle)

        val result = listOf(
            Anchor(pointA, 1f, setOf(KeyId(KeyEvent.KEYCODE_BUTTON_X)), buttonSize),
            Anchor(pointB, 1f, setOf(KeyId(KeyEvent.KEYCODE_BUTTON_Y)), buttonSize),
            Anchor(pointC, 1f, setOf(KeyId(KeyEvent.KEYCODE_BUTTON_A)), buttonSize),
            Anchor(pointD, 1f, setOf(KeyId(KeyEvent.KEYCODE_BUTTON_B)), buttonSize),
        )

        result
    }
}
