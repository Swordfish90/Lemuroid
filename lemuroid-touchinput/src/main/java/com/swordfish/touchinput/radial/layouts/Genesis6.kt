package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonSelect
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.LemuroidButton
import com.swordfish.touchinput.radial.utils.buildCentral6ButtonsAnchors
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.anchors.Anchor
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId

@Composable
fun JamPadScope.Genesis6Left(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            SecondaryButtonSelect(position = 0)
            SecondaryButtonStart(position = 1)
            SecondaryButtonMenuPlaceholder(settings)
        }
    )
}

@Composable
fun JamPadScope.Genesis6Right(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    val centralAnchors = rememberCentralAnchorsForSixButtons(settings.rotation)

    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            LemuroidControlFaceButtons(
                primaryAnchors = centralAnchors,
                background = { },
                idsForegrounds = buildMap {
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_X)) {
                        LemuroidButton(pressed = it, label = "Y")
                    }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_L1)) {
                        LemuroidButton(pressed = it, label = "X")
                    }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_B)) {
                        LemuroidButton(pressed = it, label = "B")
                    }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_Y)) {
                        LemuroidButton  (pressed = it, label = "A")
                    }
                },
            )
        },
        secondaryDials = {
            LemuroidControlButton(
                modifier = Modifier.radialPosition(60f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_A),
                label = "C"
            )
            LemuroidControlButton(
                modifier = Modifier.radialPosition(90f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_R1),
                label = "Z"
            )
            SecondaryButtonMenu(settings)
        }
    )
}

@Composable
private fun rememberCentralAnchorsForSixButtons(rotation: Float): List<Anchor<KeyId>> {
    return remember(rotation) {
        buildCentral6ButtonsAnchors(
            rotation,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_Y,
        )
    }
}
