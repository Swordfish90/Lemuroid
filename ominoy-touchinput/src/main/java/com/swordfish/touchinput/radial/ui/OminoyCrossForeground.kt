package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import com.swordfish.touchinput.radial.LocalOminoyPadTheme
import gg.padkit.ui.DefaultCrossForeground

@Composable
fun OminoyCrossForeground(
    allowDiagonals: Boolean,
    directionState: State<Offset>,
) {
    DefaultCrossForeground(
        modifier = Modifier.fillMaxSize(),
        directionState = directionState,
        allowDiagonals = allowDiagonals,
        leftDial = {
            OminoyCrossButton(it, Icons.Default.KeyboardArrowLeft)
        },
        rightDial = {
            OminoyCrossButton(it, Icons.Default.KeyboardArrowRight)
        },
        topDial = {
            OminoyCrossButton(it, Icons.Default.KeyboardArrowUp)
        },
        bottomDial = {
            OminoyCrossButton(it, Icons.Default.KeyboardArrowDown)
        },
        foregroundComposite = {
            OminoyCompositeForeground(it)
        },
    )
}

@Composable
private fun OminoyCrossButton(
    pressedState: State<Boolean>,
    imageVector: ImageVector,
) {
    OminoyButtonForeground(
        pressed = pressedState,
        label = { },
        icon = {
            Icon(
                modifier = Modifier.size(maxWidth * 0.5f, maxHeight * 0.5f),
                imageVector = imageVector,
                contentDescription = "",
                tint = LocalOminoyPadTheme.current.icons(pressedState.value),
            )
        },
    )
}
