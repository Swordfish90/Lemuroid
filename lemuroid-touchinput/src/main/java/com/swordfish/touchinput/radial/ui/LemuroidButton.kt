package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable

@Composable
fun LemuroidButton(
    pressed: Boolean,
    label: String? = null
) {
    Box {
        LemuroidControlBackground()
        LemuroidButtonForeground(
            pressed = pressed,
            label = label,
        )
    }
}
