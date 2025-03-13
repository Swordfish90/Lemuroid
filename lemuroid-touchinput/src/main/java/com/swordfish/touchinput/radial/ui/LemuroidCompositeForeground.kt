package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidCompositeForeground(pressed: Boolean) {
    val theme = LocalLemuroidPadTheme.current
    TranslucentSurface(
        modifier = Modifier.fillMaxSize(),
        fillColor = theme.foregroundFill(pressed),
        shadowWidth = theme.foregroundShadowWidth,
        shadowColor = theme.foregroundShadow,
    )
}
