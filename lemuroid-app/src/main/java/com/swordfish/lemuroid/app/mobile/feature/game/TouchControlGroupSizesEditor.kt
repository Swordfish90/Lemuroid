package com.swordfish.lemuroid.app.mobile.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.app.mobile.shared.compose.TouchControlSlider
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Settings

@Composable
internal fun TouchControlGroupSizesEditor(
    settings: Settings,
    onChange: (Settings) -> Unit,
) {
    TouchControlSlider(
        stringResource(R.string.touch_customize_left_scale),
        settings.leftScale,
        { onChange(settings.copy(leftScale = it)) },
        valueRange = 0.5f..1.5f,
    )
    TouchControlSlider(
        stringResource(R.string.touch_customize_right_scale),
        settings.rightScale,
        { onChange(settings.copy(rightScale = it)) },
        valueRange = 0.5f..1.5f,
    )
}
