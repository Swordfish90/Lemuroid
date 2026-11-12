package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalOminoyPadTheme

@Composable
fun OminoyCentralButton(
    pressedState: State<Boolean>,
    label: String? = null,
) {
    val theme = LocalOminoyPadTheme.current
    Box(modifier = Modifier.padding(theme.padding)) {
        OminoyControlBackground()
        OminoyButtonForeground(
            pressed = pressedState,
            label = label,
        )
    }
}
