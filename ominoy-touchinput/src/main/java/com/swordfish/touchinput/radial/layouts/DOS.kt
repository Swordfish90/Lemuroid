package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.controls.OminoyControlButton
import com.swordfish.touchinput.radial.controls.OminoyControlCross
import com.swordfish.touchinput.radial.controls.OminoyControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.layouts.shared.SecondaryAnalogLeft
import com.swordfish.touchinput.radial.layouts.shared.SecondaryAnalogRight
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonFastForward
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL1
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL2
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR1
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR2
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonSelect
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.OminoyButtonForeground
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun PadKitScope.DOSLeft(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { OminoyControlCross(id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            SecondaryButtonL1()
            SecondaryButtonL2()
            SecondaryButtonSelect(position = 2)
            SecondaryAnalogLeft()
            OminoyControlButton(
                modifier =
                    Modifier.radialPosition(
                        -120f - 2f * settings.rotation * TouchControllerSettingsManager.MAX_ROTATION,
                    ),
                id = Id.Key(KeyEvent.KEYCODE_BUTTON_THUMBL),
                icon = R.drawable.button_keyboard,
            )
        },
    )
}

@Composable
fun PadKitScope.DOSRight(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
) {
    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            OminoyControlFaceButtons(
                ids =
                    persistentListOf(
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A),
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B),
                        Id.Key(KeyEvent.KEYCODE_BUTTON_Y),
                        Id.Key(KeyEvent.KEYCODE_BUTTON_X),
                    ),
                idsForegrounds =
                    persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { OminoyButtonForeground(pressed = it, label = "A") },
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { OminoyButtonForeground(pressed = it, label = "B") },
                        Id.Key(KeyEvent.KEYCODE_BUTTON_Y) to { OminoyButtonForeground(pressed = it, label = "Y") },
                        Id.Key(KeyEvent.KEYCODE_BUTTON_X) to { OminoyButtonForeground(pressed = it, label = "X") },
                    ),
            )
        },
        secondaryDials = {
            SecondaryButtonR1()
            SecondaryButtonR2()
            SecondaryButtonStart(position = 2)
            SecondaryAnalogRight()
            SecondaryButtonMenu(settings)
            SecondaryButtonFastForward(settings)
        },
    )
}
