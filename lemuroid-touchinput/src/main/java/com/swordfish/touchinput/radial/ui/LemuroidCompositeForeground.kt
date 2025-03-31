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
        fillColor = theme.compositeFill(pressed),
        strokeColor = theme.compositeStroke(pressed),
        strokeWidth = theme.level2StrokeWidth,
        shadowWidth = theme.level2ShadowWidth,
        shadowColor = theme.level2Shadow,
    )
}
