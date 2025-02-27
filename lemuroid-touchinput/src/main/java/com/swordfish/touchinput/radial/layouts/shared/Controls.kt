package com.swordfish.touchinput.radial.layouts.shared

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.controls.LemuroidControlAnalog
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.ids.ContinuousDirectionId
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonSelect(position: Int = 0) {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f - 30f * position),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_SELECT),
        icon = R.drawable.button_select,
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonL1() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(90f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_L1),
        label = "L1",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonL2() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_L2),
        label = "L2",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonR1() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(90f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_R1),
        label = "R1"
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonR2() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_R2),
        label = "R2",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonL() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_L1),
        label = "L",
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonR() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_R1),
        label = "R"
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonStart(position: Int = 0) {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f + 30f * position),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_START),
        icon = R.drawable.button_start
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonMenu(settings: TouchControllerSettingsManager.Settings) {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(-60f + 2f * settings.rotation * TouchControllerSettingsManager.MAX_ROTATION),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_MODE),
        icon = R.drawable.button_menu
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonMenuPlaceholder(settings: TouchControllerSettingsManager.Settings) {
    Box(modifier = Modifier.radialPosition(-120f - 2f * settings.rotation * TouchControllerSettingsManager.MAX_ROTATION))
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryAnalogLeft() {
    LemuroidControlAnalog(
        modifier = Modifier
            .radialPosition(-80f)
            .radialScale(2.2f),
        id = ContinuousDirectionId(ComposeTouchLayouts.MOTION_SOURCE_LEFT_STICK),
        analogPressId = KeyId(KeyEvent.KEYCODE_BUTTON_THUMBL)
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryAnalogRight() {
    LemuroidControlAnalog(
        modifier = Modifier
            .radialPosition(+80f -180f)
            .radialScale(2.2f),
        id = ContinuousDirectionId(ComposeTouchLayouts.MOTION_SOURCE_RIGHT_STICK),
        analogPressId = KeyId(KeyEvent.KEYCODE_BUTTON_THUMBR)
    )
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonCoin() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = KeyId(KeyEvent.KEYCODE_BUTTON_SELECT),
        icon = R.drawable.button_coin,
    )
}

object ComposeTouchLayouts {
    const val MOTION_SOURCE_DPAD = 0
    const val MOTION_SOURCE_LEFT_STICK = 1
    const val MOTION_SOURCE_RIGHT_STICK = 2
    const val MOTION_SOURCE_DPAD_AND_LEFT_STICK = 3
    const val MOTION_SOURCE_RIGHT_DPAD = 4
}
