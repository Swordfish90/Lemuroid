package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import com.swordfish.touchinput.radial.ui.LemuroidCompositeForeground
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.anchors.ButtonAnchor
import gg.jam.jampadcompose.controls.ControlFaceButtons
import gg.jam.jampadcompose.ids.KeyId

context(JamPadScope)
@Composable
fun LemuroidControlFaceButtons(
    modifier: Modifier = Modifier,
    rotationInDegrees: Float = 0f,
    ids: List<KeyId>,
    includeComposite: Boolean = true,
    background: @Composable () -> Unit = { LemuroidControlBackground() },
    idsForegrounds: Map<KeyId, @Composable (Boolean) -> Unit>
) {
    val theme = LocalLemuroidPadTheme.current

    ControlFaceButtons(
        modifier = modifier.padding(theme.padding),
        includeComposite = includeComposite,
        ids = ids,
        rotationInDegrees = rotationInDegrees,
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = background,
        foregroundComposite = { LemuroidCompositeForeground(it) },
    )
}

context(JamPadScope)
@Composable
fun LemuroidControlFaceButtons(
    modifier: Modifier = Modifier,
    mainAnchors: List<ButtonAnchor>,
    background: @Composable () -> Unit = { LemuroidControlBackground() },
    idsForegrounds: Map<KeyId, @Composable (Boolean) -> Unit>
) {
    val theme = LocalLemuroidPadTheme.current

    ControlFaceButtons(
        modifier = modifier.padding(theme.padding),
        mainButtonAnchors = mainAnchors,
        compositeButtonAnchors = emptyList(),
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = background,
        foregroundComposite = { LemuroidCompositeForeground(it) },
    )
}
