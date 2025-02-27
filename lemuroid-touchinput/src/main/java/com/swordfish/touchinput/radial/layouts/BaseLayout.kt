package com.swordfish.touchinput.radial.layouts

import LayoutRadial
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope

context(JamPadScope)
@Composable
fun BaseLayoutLeft(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
    primaryDial: @Composable () -> Unit,
    secondaryDials: @Composable LayoutRadialSecondaryDialsScope.() -> Unit,
) {
    LayoutRadial(
        modifier =
            modifier
                .absolutePadding(
                    left = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginX,
                    bottom = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginY
                )
                .padding(LocalLemuroidPadTheme.current.padding),
        primaryDial = primaryDial,
        secondaryDials = secondaryDials,
        primaryDialMaxSize = 160.dp * lerp(TouchControllerSettingsManager.MIN_SCALE, TouchControllerSettingsManager.MAX_SCALE, settings.scale),
        secondaryDialsBaseRotationInDegrees = settings.rotation * TouchControllerSettingsManager.MAX_ROTATION
    )
}

context(JamPadScope)
@Composable
fun BaseLayoutRight(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
    primaryDial: @Composable () -> Unit,
    secondaryDials: @Composable LayoutRadialSecondaryDialsScope.() -> Unit,
) {
    LayoutRadial(
        modifier =
            modifier
                .absolutePadding(
                    right = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginX,
                    bottom = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginY
                )
                .padding(LocalLemuroidPadTheme.current.padding),
        primaryDial = primaryDial,
        secondaryDials = secondaryDials,
        primaryDialMaxSize = 160.dp * lerp(TouchControllerSettingsManager.MIN_SCALE, TouchControllerSettingsManager.MAX_SCALE, settings.scale),
        secondaryDialsBaseRotationInDegrees = -settings.rotation * TouchControllerSettingsManager.MAX_ROTATION
    )
}
