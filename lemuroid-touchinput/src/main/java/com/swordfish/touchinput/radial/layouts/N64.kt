package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.SecondaryAnalogLeft
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.LemuroidCrossForegroundAlternate
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId

@Composable
fun JamPadScope.N64Left(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            SecondaryAnalogLeft()
            SecondaryButtonL()
            LemuroidControlButton(
                modifier = Modifier.radialPosition(60f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_L2),
                label = "Z",
            )
            SecondaryButtonMenuPlaceholder(settings)
        }
    )
}

@Composable
fun JamPadScope.N64Right(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            LemuroidControlFaceButtons(
                rotationInDegrees = 45f,
                ids = listOf(
                    KeyId(KeyEvent.KEYCODE_BUTTON_A),
                    KeyId(KeyEvent.KEYCODE_BUTTON_B),
                    KeyId(KeyEvent.KEYCODE_BUTTON_L2),
                ),
                idsForegrounds = buildMap {
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_A)) { LemuroidButtonForeground(it, label = "A") }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_B)) { LemuroidButtonForeground(it, label = "B") }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_L2)) { LemuroidButtonForeground(it, label = "Z") }
                },
            )
        },
        secondaryDials = {
            SecondaryButtonR()
            SecondaryButtonStart(position = 2)
            LemuroidControlCross(
                modifier = Modifier
                    .radialPosition(+80f -180f)
                    .radialScale(2.2f),
                allowDiagonals = false,
                id = DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_RIGHT_STICK),
                foreground = { LemuroidCrossForegroundAlternate(it) }
            )
            SecondaryButtonMenu(settings)
        }
    )
}
