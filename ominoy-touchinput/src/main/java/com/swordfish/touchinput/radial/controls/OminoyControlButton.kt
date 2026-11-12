package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.touchinput.radial.LocalOminoyPadTheme
import com.swordfish.touchinput.radial.settings.isTouchControlVisible
import com.swordfish.touchinput.radial.settings.touchControlTransform
import com.swordfish.touchinput.radial.ui.OminoyButtonForeground
import com.swordfish.touchinput.radial.ui.OminoyControlBackground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlButton
import gg.padkit.ids.Id
import gg.padkit.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun OminoyControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
) {
    if (!isTouchControlVisible(id)) return
    val theme = LocalOminoyPadTheme.current
    ControlButton(
        modifier = modifier.touchControlTransform(id).padding(theme.padding),
        id = id,
        foreground = { OminoyButtonForeground(pressed = it, icon = icon, label = label) },
        background = { OminoyControlBackground() },
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun OminoyShoulderControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String,
    mirrored: Boolean,
    pill: Boolean = false,
) {
    if (!isTouchControlVisible(id)) return
    val theme = LocalOminoyPadTheme.current
    ControlButton(
        modifier = modifier.touchControlTransform(id).padding(theme.padding),
        id = id,
        background = { pressed ->
            Canvas(Modifier.fillMaxSize()) {
                if (size.width <= 0f || size.height <= 0f) return@Canvas
                fun x(value: Float) = (if (mirrored) 1f - value else value) * size.width
                fun y(value: Float) = value * size.height

                if (pill) {
                    // L slopes down toward the outer-left edge; R mirrors that tilt.
                    rotate(degrees = if (mirrored) 7f else -7f, pivot = center) {
                        val radius = size.height * 0.46f
                        drawRoundRect(
                            color = theme.compositeFill(pressed.value),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                        )
                        drawRoundRect(
                            color = theme.outlineHaloColor,
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                            style = Stroke(width = theme.outlineHaloWidth.toPx()),
                        )
                        drawRoundRect(
                            color = theme.outlineColor,
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                            style = Stroke(width = theme.outlineWidth.toPx()),
                        )
                    }
                    return@Canvas
                }

                val shoulder = Path().apply {
                    moveTo(x(0.04f), y(0.47f))
                    cubicTo(x(0.01f), y(0.31f), x(0.10f), y(0.12f), x(0.27f), y(0.08f))
                    cubicTo(x(0.45f), y(0.03f), x(0.59f), y(0.19f), x(0.72f), y(0.30f))
                    cubicTo(x(0.84f), y(0.40f), x(0.98f), y(0.43f), x(0.99f), y(0.58f))
                    cubicTo(x(1.00f), y(0.77f), x(0.87f), y(0.92f), x(0.70f), y(0.91f))
                    cubicTo(x(0.55f), y(0.90f), x(0.48f), y(0.78f), x(0.37f), y(0.74f))
                    cubicTo(x(0.26f), y(0.70f), x(0.18f), y(0.82f), x(0.10f), y(0.74f))
                    cubicTo(x(0.04f), y(0.67f), x(0.02f), y(0.56f), x(0.04f), y(0.47f))
                    close()
                }
                drawPath(shoulder, theme.compositeFill(pressed.value))
                drawPath(
                    shoulder,
                    theme.outlineHaloColor,
                    style = Stroke(width = theme.outlineHaloWidth.toPx()),
                )
                drawPath(
                    shoulder,
                    theme.outlineColor,
                    style = Stroke(width = theme.outlineWidth.toPx()),
                )
            }
        },
        foreground = { pressed ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = theme.icons(pressed.value),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        },
    )
}
