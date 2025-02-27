package com.swordfish.touchinput.radial.controls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.controls.ControlAnalog
import gg.jam.jampadcompose.ids.ContinuousDirectionId
import gg.jam.jampadcompose.ids.KeyId

context(JamPadScope)
@Composable
fun LemuroidControlAnalog(
    modifier: Modifier = Modifier,
    analogPressId: KeyId? = null,
    id: ContinuousDirectionId,
) {
    ControlAnalog(
        modifier = modifier,
        id = id,
        analogPressId = analogPressId,
        background = { LemuroidControlBackground() },
        foreground = { LemuroidButtonForeground(it, scale = 1.0f) },
    )
}
