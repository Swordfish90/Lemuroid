package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidControlBackground(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current
    TranslucentSurface(
        modifier = modifier.fillMaxSize(),
        fillColor = theme.backgroundFill,
        shadowWidth = theme.backgroundShadowWidth,
        shadowColor = theme.backgroundShadow,
    )
}
