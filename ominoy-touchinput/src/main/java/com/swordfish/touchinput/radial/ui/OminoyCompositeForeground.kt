package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalOminoyPadTheme

@Composable
fun OminoyCompositeForeground(pressed: State<Boolean>) {
    val theme = LocalOminoyPadTheme.current
    GlassSurface(
        modifier = Modifier.fillMaxSize(),
        fillColor = theme.compositeFill(pressed.value),
        shadowColor = theme.level2Shadow,
        shadowWidth = theme.level2ShadowWidth,
        outlineColor = theme.outlineColor,
        outlineWidth = theme.outlineWidth,
        outlineHaloColor = theme.outlineHaloColor,
        outlineHaloWidth = theme.outlineHaloWidth,
    )
}
