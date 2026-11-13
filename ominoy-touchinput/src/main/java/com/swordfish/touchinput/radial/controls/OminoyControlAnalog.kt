package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalOminoyPadTheme
import com.swordfish.touchinput.radial.settings.isTouchControlVisible
import com.swordfish.touchinput.radial.settings.touchControlTransform
import com.swordfish.touchinput.radial.ui.OminoyButtonForeground
import com.swordfish.touchinput.radial.ui.OminoyControlBackground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlAnalog
import gg.padkit.ids.Id

context(PadKitScope)
@Composable
fun OminoyControlAnalog(
    modifier: Modifier = Modifier,
    analogPressId: Id.Key? = null,
    id: Id.ContinuousDirection,
) {
    if (!isTouchControlVisible(id)) return
    val theme = LocalOminoyPadTheme.current
    Box(
        modifier = modifier.touchControlTransform(id).padding(theme.padding),
        contentAlignment = Alignment.Center,
    ) {
        ControlAnalog(
            id = id,
            analogPressId = analogPressId,
            background = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { OminoyControlBackground(Modifier.fillMaxSize(0.8f)) }
            },
            foreground = { OminoyButtonForeground(pressed = it) },
        )
    }
}
