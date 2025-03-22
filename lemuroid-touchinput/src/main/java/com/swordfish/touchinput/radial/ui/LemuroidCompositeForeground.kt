package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidCompositeForeground(pressed: Boolean) {
    val theme = LocalLemuroidPadTheme.current
    GlassSurface(
        modifier = Modifier.fillMaxSize(),
        fillColor = theme.foregroundFill(pressed),
        strokeColor = theme.foregroundStroke(pressed),
        shadowWidth = theme.foregroundShadowWidth,
        shadowColor = theme.foregroundShadow,
    )
}
