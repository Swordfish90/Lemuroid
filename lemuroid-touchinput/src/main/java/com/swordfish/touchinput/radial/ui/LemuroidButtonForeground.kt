package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.times
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import gg.jam.jampadcompose.utils.GeometryUtils.textUnit

@Composable
fun LemuroidButtonForeground(
    modifier: Modifier = Modifier,
    pressed: Boolean,
    label: (@Composable BoxWithConstraintsScope.() -> Unit),
    icon: (@Composable BoxWithConstraintsScope.() -> Unit),
    scale: Float = 0.9f,
) {
    val theme = LocalLemuroidPadTheme.current

    GlassSurface(
        modifier = modifier.fillMaxSize().padding(2.0 * theme.padding),
        scale = scale,
        fillColor = theme.foregroundFill(pressed),
        strokeColor = theme.foregroundStroke(pressed),
        shadowWidth = theme.level2ShadowWidth,
        shadowColor = theme.level2Shadow,
        strokeWidth = theme.level2StrokeWidth
    ) {
        icon()
        label()
    }
}

@Composable
fun LemuroidButtonForeground(
    modifier: Modifier = Modifier,
    pressed: Boolean,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.5f,
    scale: Float = 0.9f,
) {
    LemuroidButtonForeground(
        modifier = modifier,
        scale = scale,
        pressed = pressed,
        label = { LemuroidButtonForegroundLabel(label, pressed) },
        icon = { LemuroidButtonForegroundIcon(icon, iconScale, pressed) }
    )
}

@Composable
private fun BoxWithConstraintsScope.LemuroidButtonForegroundIcon(
    icon: Int?,
    iconScale: Float,
    pressed: Boolean
) {
    if (icon == null) return

    Icon(
        modifier = Modifier.size(maxWidth * iconScale, maxHeight * iconScale),
        painter = painterResource(icon),
        contentDescription = "",
        tint = LocalLemuroidPadTheme.current.icons(pressed),
    )
}

@Composable
private fun BoxWithConstraintsScope.LemuroidButtonForegroundLabel(label: String?, pressed: Boolean) {
    if (label == null) return

    val fontSize = minOf(maxHeight * 0.5f * 0.75f, maxWidth / label.length * 0.75f)

    Text(
        modifier = Modifier.wrapContentSize(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        text = label,
        color = LocalLemuroidPadTheme.current.icons(pressed),
        fontSize = fontSize.textUnit(),
    )
}
