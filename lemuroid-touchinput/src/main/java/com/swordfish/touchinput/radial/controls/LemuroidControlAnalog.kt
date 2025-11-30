package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlAnalog
import gg.padkit.ids.Id

context(PadKitScope)
@Composable
fun LemuroidControlAnalog(
    modifier: Modifier = Modifier,
    analogPressId: Id.Key? = null,
    id: Id.ContinuousDirection,
) {
    val theme = LocalLemuroidPadTheme.current
    Box(
        modifier = modifier.padding(theme.padding),
        contentAlignment = Alignment.Center,
    ) {
        ControlAnalog(
            id = id,
            analogPressId = analogPressId,
            background = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { LemuroidControlBackground(Modifier.fillMaxSize(0.8f)) }
            },
            foreground = { LemuroidButtonForeground(pressed = it) },
        )
    }
}
