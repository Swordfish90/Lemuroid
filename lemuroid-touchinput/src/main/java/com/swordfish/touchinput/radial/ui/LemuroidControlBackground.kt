package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidControlBackground(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current
    GlassSurface(
        modifier = modifier.fillMaxSize().padding(theme.padding),
        fillColor = theme.backgroundFill,
        strokeColor = theme.backgroundStroke,
        shadowWidth = theme.backgroundShadowWidth,
        shadowColor = theme.backgroundShadow,
    )
}
