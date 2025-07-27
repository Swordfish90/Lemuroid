package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidCompositeForeground(pressed: State<Boolean>) {
    val theme = LocalLemuroidPadTheme.current
    GlassSurface(
        modifier = Modifier.fillMaxSize(),
        fillColor = theme.compositeFill(pressed.value),
        strokeColor = theme.compositeStroke(pressed.value),
        strokeWidth = theme.level2StrokeWidth,
        shadowWidth = theme.level2ShadowWidth,
        shadowColor = theme.level2Shadow,
    )
}
