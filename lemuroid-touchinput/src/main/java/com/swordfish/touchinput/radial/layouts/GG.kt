package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun PadKitScope.GGLeft(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = { LemuroidControlCross(id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            Box(modifier = Modifier.radialPosition(120f))
            SecondaryButtonMenuPlaceholder(settings)
        }
    )
}

@Composable
fun PadKitScope.GGRight(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            LemuroidControlFaceButtons(
                rotationInDegrees = -30f,
                ids = persistentListOf(
                    Id.Key(KeyEvent.KEYCODE_BUTTON_A),
                    Id.Key(KeyEvent.KEYCODE_BUTTON_B),
                ),
                idsForegrounds = persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
                    Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { LemuroidButtonForeground(pressed = it, label = "2") },
                    Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { LemuroidButtonForeground(pressed = it, label = "1") },
                ),
            )
        },
        secondaryDials = {
            SecondaryButtonStart()
            SecondaryButtonMenu(settings)
        }
    )
}
