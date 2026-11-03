package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

val LocalTouchControlBackgroundVisible = compositionLocalOf { true }

@Composable
fun LemuroidControlBackground(modifier: Modifier = Modifier) {
    if (!LocalTouchControlBackgroundVisible.current) return

    val theme = LocalLemuroidPadTheme.current
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
