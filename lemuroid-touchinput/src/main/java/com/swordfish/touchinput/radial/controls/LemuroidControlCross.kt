package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import com.swordfish.touchinput.radial.ui.LemuroidCrossForeground
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.controls.ControlCross
import gg.jam.jampadcompose.ids.DiscreteDirectionId

context(JamPadScope)
@Composable
fun LemuroidControlCross(
    id: DiscreteDirectionId,
    allowDiagonals: Boolean = true,
    background: @Composable () -> Unit = {
        LemuroidControlBackground()
    },
    foreground: @Composable (Offset) -> Unit = {
        LemuroidCrossForeground(
            allowDiagonals = allowDiagonals,
            direction = it
        )
    },
    modifier: Modifier = Modifier
) {
    val theme = LocalLemuroidPadTheme.current
    ControlCross(
        modifier = modifier.padding(theme.padding),
        id = id,
        allowDiagonals = allowDiagonals,
        background = background,
        foreground = foreground
    )
}
