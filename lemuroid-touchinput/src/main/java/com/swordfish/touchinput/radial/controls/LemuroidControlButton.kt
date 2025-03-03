package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
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
    val theme = LocalLemuroidPadTheme.current

    ControlButton(
        modifier = modifier.padding(theme.padding),
        id = id,
        foreground = { LemuroidButtonForeground(it, icon = icon, label = label) },
        background = { LemuroidControlBackground() }
    )
}
