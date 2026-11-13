package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.swordfish.touchinput.controller.R
import gg.padkit.ui.DefaultCrossForeground

@Composable
fun OminoyCrossForegroundAlternate(directionState: State<Offset>) {
    DefaultCrossForeground(
        modifier = Modifier.fillMaxSize(),
        directionState = directionState,
        allowDiagonals = false,
        leftDial = {
            OminoyButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_left,
            )
        },
        rightDial = {
            OminoyButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_right,
            )
        },
        topDial = {
            OminoyButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_up,
            )
        },
        bottomDial = {
            OminoyButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_down,
            )
        },
        foregroundComposite = {
            OminoyCompositeForeground(it)
        },
    )
}
