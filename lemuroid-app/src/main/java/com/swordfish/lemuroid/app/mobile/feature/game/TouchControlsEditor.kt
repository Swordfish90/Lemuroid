package com.swordfish.lemuroid.app.mobile.feature.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.mobile.shared.compose.TouchControlSlider
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Settings
import kotlin.math.roundToInt

@Composable
internal fun TouchControlsEditor(
    settings: Settings,
    allowRotation: Boolean,
    maxHeight: Dp,
    onChange: (Settings) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit,
) {
    BackHandler(onBack = onDone)
    var expanded by remember { mutableStateOf(true) }
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(stringResource(R.string.touch_customize_title), modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        stringResource(
                            if (expanded) R.string.touch_customize_collapse else R.string.touch_customize_expand,
                        ),
                    )
                }
                TextButton(onClick = onReset) { Text(stringResource(R.string.touch_customize_button_reset)) }
                TextButton(onClick = onDone) { Text(stringResource(R.string.touch_customize_button_done)) }
            }
            if (expanded) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(R.string.touch_customize_drag_hint))
                    TouchControlSlider(
                        stringResource(R.string.touch_customize_scale),
                        settings.scale,
                        { onChange(settings.copy(scale = it)) },
                    )
                    TouchControlSlider(
                        stringResource(R.string.touch_customize_horizontal_margin),
                        settings.marginX,
                        { onChange(settings.copy(marginX = it)) },
                    )
                    TouchControlSlider(
                        stringResource(R.string.touch_customize_vertical_margin),
                        settings.marginY,
                        { onChange(settings.copy(marginY = it)) },
                    )
                    if (allowRotation) {
                        TouchControlSlider(
                            stringResource(R.string.touch_customize_rotation),
                            settings.rotation,
                            { onChange(settings.copy(rotation = it)) },
                            valueLabel = "${(settings.rotation * 45).roundToInt()}°",
                        )
                    }
                }
            }
        }
    }
}
