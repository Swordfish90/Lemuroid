package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.swordfish.touchinput.radial.LocalOminoyPadTheme
import com.swordfish.touchinput.radial.settings.isTouchControlVisible
import com.swordfish.touchinput.radial.settings.touchControlTransform
import com.swordfish.touchinput.radial.ui.OminoyControlBackground
import com.swordfish.touchinput.radial.ui.OminoyCrossForeground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlCross
import gg.padkit.ids.Id

context(PadKitScope)
@Composable
fun OminoyControlCross(
    modifier: Modifier = Modifier,
    id: Id.DiscreteDirection,
    allowDiagonals: Boolean = true,
    background: @Composable () -> Unit = {
        OminoyControlBackground()
    },
    foreground: @Composable (State<Offset>) -> Unit = {
        OminoyCrossForeground(
            allowDiagonals = allowDiagonals,
            directionState = it,
        )
    },
) {
    if (!isTouchControlVisible(id)) return
    val theme = LocalOminoyPadTheme.current
    ControlCross(
        modifier = modifier.touchControlTransform(id).padding(theme.padding),
        id = id,
        allowDiagonals = allowDiagonals,
        background = background,
        foreground = foreground,
    )
}
