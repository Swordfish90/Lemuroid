package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonSelect
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId

@Composable
fun JamPadScope.DesmumeLeft(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            SecondaryButtonL()
            SecondaryButtonSelect(position = 2)
            SecondaryButtonMenuPlaceholder(settings)
            LemuroidControlButton(
                modifier = Modifier.radialPosition(-120f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_THUMBL),
                icon = R.drawable.button_mic
            )
            LemuroidControlButton(
                modifier = Modifier.radialPosition(-60f),
                id = KeyId(KeyEvent.KEYCODE_BUTTON_L2),
                icon = R.drawable.button_close_screen
            )
        }
    )
}

@Composable
fun JamPadScope.DesmumeRight(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            LemuroidControlFaceButtons(
                ids = listOf(
                    KeyId(KeyEvent.KEYCODE_BUTTON_A),
                    KeyId(KeyEvent.KEYCODE_BUTTON_B),
                    KeyId(KeyEvent.KEYCODE_BUTTON_Y),
                    KeyId(KeyEvent.KEYCODE_BUTTON_X),
                ),
                idsForegrounds = buildMap {
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_A)) { LemuroidButtonForeground(it, label = "A") }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_B)) { LemuroidButtonForeground(it, label = "B") }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_Y)) { LemuroidButtonForeground(it, label = "Y") }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_X)) { LemuroidButtonForeground(it, label = "X") }
                },
            )
        },
        secondaryDials = {
            SecondaryButtonR()
            SecondaryButtonStart(position = 2)
            SecondaryButtonMenu(settings)
            Box(modifier = Modifier.fillMaxSize().radialPosition(-120f))
        }
    )
}
