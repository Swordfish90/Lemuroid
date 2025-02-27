package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import gg.jam.jampadcompose.utils.GeometryUtils.textUnit

@Composable
fun LemuroidButtonForeground(
    pressed: Boolean,
    label: (@Composable BoxWithConstraintsScope.() -> Unit),
    icon: (@Composable BoxWithConstraintsScope.() -> Unit),
    scale: Float = 0.75f,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val theme = LocalLemuroidPadTheme.current

        Canvas(modifier = Modifier.size(maxWidth * scale, maxHeight * scale)) {
            val radius = size.minDimension / 2
            val borderWidth = theme.foregroundBorderWidth.toPx()

            drawCircle(
                color = theme.foregroundFill(pressed),
                radius = radius - borderWidth / 2
            )

            drawCircle(
                color = theme.foregroundStroke(pressed),
                radius = radius,
                style = Stroke(width = borderWidth)
            )
        }

        icon()
        label()
    }
}

@Composable
fun LemuroidButtonForeground(
    pressed: Boolean,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.5f,
    scale: Float = 0.75f,
) {
    LemuroidButtonForeground(
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
        tint = LocalLemuroidPadTheme.current.foregroundStroke(pressed),
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
        color = LocalLemuroidPadTheme.current.foregroundStroke(pressed),
        fontSize = fontSize.textUnit(),
    )
}
