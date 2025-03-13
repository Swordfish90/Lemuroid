package com.swordfish.touchinput.radial.controls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.controls.ControlButton
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun LemuroidControlButton(
    modifier: Modifier = Modifier,
    id: KeyId,
    label: String? = null,
    icon: Int? = null,
) {
    ControlButton(
        modifier = modifier,
        id = id,
        foreground = { LemuroidButtonForeground(pressed = it, icon = icon, label = label) },
        background = { LemuroidControlBackground() }
    )
}
