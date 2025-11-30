package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.swordfish.lemuroid.common.compose.textUnit
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: (@Composable BoxWithConstraintsScope.() -> Unit),
    icon: (@Composable BoxWithConstraintsScope.() -> Unit),
) {
    val theme = LocalLemuroidPadTheme.current

    GlassSurface(
        modifier = modifier.fillMaxSize().padding(theme.foregroundPadding),
        fillColor = theme.foregroundFill(pressed.value),
        shadowColor = theme.level3Shadow,
        shadowWidth = theme.level3ShadowWidth,
        content = {
            icon()
            label()
        },
    )
}

@Composable
fun LemuroidButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.5f,
    labelScale: Float = 1.0f,
) {
    LemuroidButtonForeground(
        modifier = modifier,
        pressed = pressed,
        label = { LemuroidButtonForegroundLabel(label, labelScale, pressed) },
        icon = { LemuroidButtonForegroundIcon(icon, iconScale, pressed) },
    )
}

@Composable
private fun BoxWithConstraintsScope.LemuroidButtonForegroundIcon(
    icon: Int?,
    scale: Float,
    pressedState: State<Boolean>,
) {
    if (icon == null) return

    Icon(
        modifier = Modifier.size(maxWidth * scale, maxHeight * scale),
        painter = painterResource(icon),
        contentDescription = "",
        tint = LocalLemuroidPadTheme.current.icons(pressedState.value),
    )
}

@Composable
private fun BoxWithConstraintsScope.LemuroidButtonForegroundLabel(
    label: String?,
    scale: Float,
    pressedState: State<Boolean>,
) {
    if (label == null) return
    val fontSize = minOf(maxHeight * 0.5f * scale, maxWidth / label.length * scale)
    Text(
        modifier = Modifier.wrapContentSize(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        text = label,
        color = LocalLemuroidPadTheme.current.icons(pressedState.value),
        fontSize = fontSize.textUnit(),
    )
}
