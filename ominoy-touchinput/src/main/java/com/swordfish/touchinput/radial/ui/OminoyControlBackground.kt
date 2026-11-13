package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalOminoyPadTheme

val LocalTouchControlBackgroundVisible = compositionLocalOf { true }

@Composable
fun OminoyControlBackground(modifier: Modifier = Modifier) {
    if (!LocalTouchControlBackgroundVisible.current) return

    val theme = LocalOminoyPadTheme.current
    GlassSurface(
        modifier = modifier.fillMaxSize(),
        fillColor = theme.level1Fill,
        shadowColor = theme.level1Shadow,
        shadowWidth = theme.level1ShadowWidth,
        outlineColor = theme.outlineColor,
        outlineWidth = theme.outlineWidth,
        outlineHaloColor = theme.outlineHaloColor,
        outlineHaloWidth = theme.outlineHaloWidth,
    )
}
