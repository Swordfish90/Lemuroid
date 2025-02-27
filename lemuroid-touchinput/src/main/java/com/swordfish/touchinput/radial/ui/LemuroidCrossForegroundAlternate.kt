package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.swordfish.touchinput.controller.R
import gg.jam.jampadcompose.ui.DefaultCrossForeground

@Composable
fun LemuroidCrossForegroundAlternate(direction: Offset) {
    DefaultCrossForeground(
        modifier = Modifier.fillMaxSize(),
        direction = direction,
        allowDiagonals = false,
        leftDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_left
            )
        },
        rightDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_right
            )
        },
        topDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_up
            )
        },
        bottomDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_down
            )
        },
        foregroundComposite = {
            LemuroidCompositeForeground(it)
        }
    )
}
