package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.SecondaryAnalogLeft
import com.swordfish.touchinput.radial.layouts.shared.SecondaryAnalogRight
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL1
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL2
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR1
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR2
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonSelect
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId

@Composable
fun JamPadScope.PSXDualShockLeft(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            SecondaryButtonL1()
            SecondaryButtonL2()
            SecondaryButtonSelect(position = 2)
            SecondaryButtonMenuPlaceholder(settings)
            SecondaryAnalogLeft()
        }
    )
}

@Composable
fun JamPadScope.PSXDualShockRight(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
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
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_A)) { LemuroidButtonForeground(it, icon = R.drawable.psx_circle) }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_B)) { LemuroidButtonForeground(it, icon = R.drawable.psx_cross) }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_Y)) { LemuroidButtonForeground(it, icon = R.drawable.psx_square) }
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_X)) {
                        LemuroidButtonForeground(
                            it,
                            icon = R.drawable.psx_triangle
                        )
                    }
                },
            )
        },
        secondaryDials = {
            SecondaryButtonR1()
            SecondaryButtonR2()
            SecondaryButtonStart(position = 2)
            SecondaryAnalogRight()
            SecondaryButtonMenu(settings)
        }
    )
}
