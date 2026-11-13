package come.asg.ominoy.app.utils.android.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import come.asg.ominoy.R
import come.asg.ominoy.app.utils.android.stringListResource
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import android.graphics.Color as AndroidColor

@Composable
fun TouchButtonAppearanceSettings() {
    val appearance =
        indexPreferenceState(
            R.string.pref_key_touch_button_appearance,
            "classic",
            stringListResource(R.array.pref_key_touch_button_appearance_values),
        )
    val red = intPreferenceState(stringResource(R.string.pref_key_touch_button_red), 120)
    val green = intPreferenceState(stringResource(R.string.pref_key_touch_button_green), 175)
    val blue = intPreferenceState(stringResource(R.string.pref_key_touch_button_blue), 255)
    val opacity = intPreferenceState(stringResource(R.string.pref_key_touch_button_opacity), 75)
    val selectedColor = Color(red.value / 255f, green.value / 255f, blue.value / 255f)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OminoySettingsList(
            state = appearance,
            title = { Text(text = stringResource(R.string.settings_title_touch_button_appearance)) },
            items = stringListResource(R.array.pref_key_touch_button_appearance_display_names),
        )
        Text(
            text = stringResource(R.string.settings_description_touch_button_appearance),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (appearance.value == 3) {
            CustomColorPickerCard(
                color = selectedColor,
                opacity = opacity.value,
                onColorChanged = { color ->
                    red.value = (color.red * 255f).toInt()
                    green.value = (color.green * 255f).toInt()
                    blue.value = (color.blue * 255f).toInt()
                },
                onOpacityChanged = { opacity.value = it },
            )
        }
    }
}

@Composable
private fun CustomColorPickerCard(
    color: Color,
    opacity: Int,
    onColorChanged: (Color) -> Unit,
    onOpacityChanged: (Int) -> Unit,
) {
    val rgbHex =
        remember(color) {
            "#%02X%02X%02X".format(
                (color.red * 255f).toInt(),
                (color.green * 255f).toInt(),
                (color.blue * 255f).toInt(),
            )
        }
    val previewTextColor = if (luminance(color) < 0.48f) Color.White else Color.Black
    val displayedColor = color.copy(alpha = opacity / 100f)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.touch_button_color_picker_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.touch_button_color_picker_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = rgbHex,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        modifier =
                            Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(displayedColor)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.titleMedium,
                            color = previewTextColor.copy(alpha = (opacity / 100f).coerceAtLeast(0.45f)),
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ColorWheel(
                    color = color,
                    onColorChanged = onColorChanged,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.touch_button_color_opacity),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(R.string.touch_button_color_opacity_percent, opacity),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = opacity.toFloat(),
                    onValueChange = { onOpacityChanged(it.toInt()) },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = color,
                            activeTrackColor = color,
                            inactiveTrackColor = color.copy(alpha = 0.24f),
                            activeTickColor = MaterialTheme.colorScheme.onPrimary,
                            inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ColorWheel(
    color: Color,
    onColorChanged: (Color) -> Unit,
) {
    val latestColor = rememberUpdatedState(color)
    val latestOnColorChanged = rememberUpdatedState(onColorChanged)
    val wheelOutlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val hsv =
        remember(color) {
            FloatArray(3).also { values ->
                AndroidColor.RGBToHSV(
                    (color.red * 255f).toInt(),
                    (color.green * 255f).toInt(),
                    (color.blue * 255f).toInt(),
                    values,
                )
            }
        }

    fun update(
        offset: Offset,
        canvasSize: Size,
    ) {
        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        val radius = minOf(canvasSize.width, canvasSize.height) / 2f
        val vector = offset - center
        val distance = hypot(vector.x, vector.y)
        if (radius <= 0f) return

        val hue =
            if (distance < 1f) {
                val current = FloatArray(3)
                val currentColor = latestColor.value
                AndroidColor.RGBToHSV(
                    (currentColor.red * 255f).toInt(),
                    (currentColor.green * 255f).toInt(),
                    (currentColor.blue * 255f).toInt(),
                    current,
                )
                current[0]
            } else {
                ((Math.toDegrees(atan2(vector.y.toDouble(), vector.x.toDouble())) + 360.0) % 360.0).toFloat()
            }
        val saturation = (distance / radius).coerceIn(0f, 1f)
        val current = FloatArray(3)
        val currentColor = latestColor.value
        AndroidColor.RGBToHSV(
            (currentColor.red * 255f).toInt(),
            (currentColor.green * 255f).toInt(),
            (currentColor.blue * 255f).toInt(),
            current,
        )
        latestOnColorChanged.value(Color.hsv(hue, saturation, current[2]))
    }

    Canvas(
        modifier =
            Modifier
                .size(224.dp)
                .semantics { contentDescription = "Hue and saturation color wheel" }
                .pointerInput(Unit) {
                    detectTapGestures { update(it, Size(size.width.toFloat(), size.height.toFloat())) }
                }.pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        update(change.position, Size(size.width.toFloat(), size.height.toFloat()))
                    }
                },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f
        val colors = (0..12).map { Color.hsv(it * 30f, 1f, 1f) }
        drawCircle(Brush.sweepGradient(colors, center), radius, center)
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.White, Color.Transparent), radius = radius, center = center),
            radius = radius,
            center = center,
        )
        drawCircle(
            color = wheelOutlineColor,
            radius = radius - 1.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )

        val angle = Math.toRadians(hsv[0].toDouble())
        val markerCenter =
            Offset(
                x = center.x + (cos(angle) * radius * hsv[1]).toFloat(),
                y = center.y + (sin(angle) * radius * hsv[1]).toFloat(),
            )
        val markerRadius = 11.dp.toPx()
        drawCircle(Color.Black.copy(alpha = 0.45f), markerRadius + 4.dp.toPx(), markerCenter)
        drawCircle(Color.White, markerRadius + 2.dp.toPx(), markerCenter)
        drawCircle(color, markerRadius, markerCenter)
    }
}

private fun luminance(color: Color): Float = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
