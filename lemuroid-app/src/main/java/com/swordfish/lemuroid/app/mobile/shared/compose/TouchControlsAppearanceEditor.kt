package com.swordfish.lemuroid.app.mobile.shared.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Appearance
import kotlin.math.roundToInt

@Composable
internal fun TouchControlsAppearanceEditor(
    appearance: Appearance,
    onChange: (Appearance) -> Unit,
    enabled: Boolean = true,
) {
    val backgroundLabel = stringResource(R.string.touch_customize_transparent_background)
    TouchControlSlider(
        label = stringResource(R.string.touch_customize_scale),
        value = appearance.scale,
        valueLabel = "${(75 + appearance.scale * 75).roundToInt()}%",
        onValueChange = { onChange(appearance.copy(scale = it)) },
        enabled = enabled,
    )
    TouchControlSlider(
        label = stringResource(R.string.touch_customize_opacity),
        value = appearance.opacity,
        onValueChange = { onChange(appearance.copy(opacity = it)) },
        valueRange = 0.1f..1f,
        enabled = enabled,
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(backgroundLabel, modifier = Modifier.weight(1f))
        Switch(
            checked = appearance.transparentBackground,
            onCheckedChange = { onChange(appearance.copy(transparentBackground = it)) },
            enabled = enabled,
            modifier = Modifier.semantics { contentDescription = backgroundLabel },
        )
    }
}
