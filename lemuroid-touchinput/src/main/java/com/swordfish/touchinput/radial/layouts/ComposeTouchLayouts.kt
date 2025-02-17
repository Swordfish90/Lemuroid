package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.ids.ContinuousDirectionId
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonSelect() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_SELECT),
        icon = R.drawable.button_select,
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonL1() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(90f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_L1),
        label = "L1",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonL2() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_L2),
        label = "L2",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonR2() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_R2),
        label = "R2",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonR1() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(90f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_R1),
        label = "R1"
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonStart() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_START),
        icon = R.drawable.button_start
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonMenu() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(-60f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_MODE),
        icon = R.drawable.button_menu
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
private fun SecondaryButtonMenuPlaceholder() {
    Box(modifier = Modifier.radialPosition(-120f))
}

@Composable
fun JamPadScope.PSXDualShockLeft(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BasePadLeft(
        settings = settings,
        modifier = modifier.absolutePadding(left = settings.marginX * 30.dp),
        primaryDial = { LemuroidControlCross(DiscreteDirectionId(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            SecondaryButtonL1()
            SecondaryButtonL2()
            SecondaryButtonSelect()
            SecondaryButtonMenuPlaceholder()
            LemuroidControlAnalog(
                modifier = Modifier
                    .radialPosition(-60f)
                    .radialScale(2f),
                id = ContinuousDirectionId(ComposeTouchLayouts.MOTION_SOURCE_LEFT_STICK),
                analogPressId = KeyId(KeyEvent.KEYCODE_BUTTON_THUMBL)
            )
        }
    )
}

@Composable
fun JamPadScope.PSXDualShockRight(modifier: Modifier = Modifier, settings: TouchControllerSettingsManager.Settings) {
    BasePadRight(
        settings = settings,
        modifier = modifier.absolutePadding(right = settings.marginX * 30.dp),
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
                    put(KeyId(KeyEvent.KEYCODE_BUTTON_X)) { LemuroidButtonForeground(it, icon = R.drawable.psx_triangle) }
                },
            )
        },
        secondaryDials = {
            SecondaryButtonR1()
            SecondaryButtonR2()
            SecondaryButtonStart()
            LemuroidControlAnalog(
                modifier = Modifier
                    .radialPosition(-120f)
                    .radialScale(2f),
                id = ContinuousDirectionId(ComposeTouchLayouts.MOTION_SOURCE_RIGHT_STICK),
                analogPressId = KeyId(KeyEvent.KEYCODE_BUTTON_THUMBR),
            )
            SecondaryButtonMenu()
        }
    )
}

object ComposeTouchLayouts {
    const val MOTION_SOURCE_DPAD = 0
    const val MOTION_SOURCE_LEFT_STICK = 1
    const val MOTION_SOURCE_RIGHT_STICK = 2
    const val MOTION_SOURCE_DPAD_AND_LEFT_STICK = 3
    const val MOTION_SOURCE_RIGHT_DPAD = 4
}
