package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId

@Composable
fun JamPadScope.Atari2600Left(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            LemuroidControlButton(
                modifier = Modifier.radialPosition(120f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_L1),
                label = "DIFF.A",
            )
            LemuroidControlButton(
                modifier = Modifier.radialPosition(60f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_L2),
                label = "DIFF.B",
            )
            SecondaryButtonMenuPlaceholder(settings)
        }
    )
}

@Composable
fun JamPadScope.Atari2600Right(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            LemuroidControlFaceButtons(
                ids = listOf(KeyId(KeyEvent.KEYCODE_BUTTON_B)),
                includeComposite = false,
                idsForegrounds = buildMap {
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_B)) { LemuroidButtonForeground(it) }
                },
            )
        },
        secondaryDials = {
            LemuroidControlButton(
                modifier = Modifier.radialPosition(60f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_START),
                label = "RESET",
            )
            LemuroidControlButton(
                modifier = Modifier.radialPosition(120f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_SELECT),
                label = "SELECT",
            )
            SecondaryButtonMenu(settings)
            Box(modifier = Modifier.fillMaxSize().radialPosition(-120f))
        }
    )
}
