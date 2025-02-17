package com.swordfish.touchinput.radial.layouts

import LayoutRadial
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPadScope
import gg.jam.jampadcompose.controls.ControlAnalog
import gg.jam.jampadcompose.controls.ControlButton
import gg.jam.jampadcompose.controls.ControlCross
import gg.jam.jampadcompose.controls.ControlFaceButtons
import gg.jam.jampadcompose.ids.ContinuousDirectionId
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope
import gg.jam.jampadcompose.ui.DefaultCrossForeground
import gg.jam.jampadcompose.utils.GeometryUtils.textUnit

private val FOREGROUND_FILL = Color(0x66333333)
private val FOREGROUND_STROKE = Color(0xAAFFFFFF)
private val BACKGROUND_FILL = Color(0x66333333)
private val BACKGROUND_STROKE = Color(0x66333333)

@Composable
fun LemuroidComposeForeground(pressed: Boolean) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape,
        color = FOREGROUND_FILL,
        border = BorderStroke(1.dp, FOREGROUND_STROKE)
    ) { }
}

@Composable
fun LemuroidButtonForeground(
    pressed: Boolean,
    label: String? = null,
    icon: Int? = null,
    scale: Float = 0.75f,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
            Modifier
                .fillMaxSize(scale)
                .aspectRatio(1f),
            shape = CircleShape,
            color = FOREGROUND_FILL,
            border = BorderStroke(2.dp, FOREGROUND_STROKE)
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.fillMaxSize(0.5f),
                    painter = painterResource(icon),
                    contentDescription = label,
                    tint = FOREGROUND_STROKE
                )
            } else if (label != null) {
                Text(
                    modifier =
                    Modifier
                        .fillMaxWidth(0.75f)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    text = label,
                    color = FOREGROUND_STROKE,
                    fontSize = (minOf(maxHeight * 0.5f, maxWidth / label.length)).textUnit(),
                )
            }
        }
    }
}

@Composable
fun LemuroidButtonBackground() {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        shape = CircleShape,
        color = BACKGROUND_FILL,
        border = BorderStroke(1.dp, BACKGROUND_STROKE)
    ) { }
}

context(JamPadScope, LayoutRadialSecondaryDialsScope)
@Composable
fun LemuroidControlButton(
    modifier: Modifier = Modifier,
    id: KeyId,
    label: String? = null,
    icon: Int? = null,
) {
    ControlButton(
        modifier = modifier,
        id = id,
        foreground = {
            LemuroidButtonForeground(it, icon = icon, label = label)
        },
        background = {
            LemuroidButtonBackground()
        }
    )
}

@Composable
fun LemuroidCrossForeground(direction: Offset) {
    DefaultCrossForeground(
        modifier = Modifier.fillMaxSize(),
        direction = direction,
        leftDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_left
            )
        },
        rightDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_right
            )
        },
        topDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_up
            )
        },
        bottomDial = {
            LemuroidButtonForeground(
                pressed = it,
                icon = R.drawable.direction_alt_foreground_down
            )
        },
        foregroundComposite = {
            LemuroidComposeForeground(it)
        }
    )
}

context(JamPadScope)
@Composable
fun LemuroidControlCross(id: DiscreteDirectionId) {
    ControlCross(
        id = id,
        background = { LemuroidButtonBackground() },
        foreground = { LemuroidCrossForeground(it) },
    )
}

context(JamPadScope)
@Composable
fun LemuroidControlAnalog(
    modifier: Modifier = Modifier,
    analogPressId: KeyId? = null,
    id: ContinuousDirectionId,
) {
    ControlAnalog(
        modifier = modifier,
        id = id,
        analogPressId = analogPressId,
        background = { LemuroidButtonBackground() },
        foreground = { LemuroidButtonForeground(it, scale = 1.0f) },
    )
}

context(JamPadScope)
@Composable
fun LemuroidControlFaceButtons(
    modifier: Modifier = Modifier,
    ids: List<KeyId>,
    idsForegrounds: Map<KeyId, @Composable (Boolean) -> Unit>
) {
    ControlFaceButtons(
        modifier = modifier,
        ids = ids,
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = { LemuroidButtonBackground() },
        foregroundComposite = { LemuroidComposeForeground(it) },
    )
}

context(JamPadScope)
@Composable
fun BasePadLeft(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
    primaryDial: @Composable () -> Unit,
    secondaryDials: @Composable LayoutRadialSecondaryDialsScope.() -> Unit,
) {
    LayoutRadial(
        modifier =
            modifier.absolutePadding(
                left = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginX,
                bottom = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginY
            ),
        primaryDial = primaryDial,
        secondaryDials = secondaryDials,
        primaryDialMaxSize = 160.dp * lerp(TouchControllerSettingsManager.MIN_SCALE, TouchControllerSettingsManager.MAX_SCALE, settings.scale),
        secondaryDialsBaseRotationInDegrees = settings.rotation * TouchControllerSettingsManager.MAX_ROTATION
    )
}

context(JamPadScope)
@Composable
fun BasePadRight(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
    primaryDial: @Composable () -> Unit,
    secondaryDials: @Composable LayoutRadialSecondaryDialsScope.() -> Unit,
) {
    LayoutRadial(
        modifier =
            modifier.absolutePadding(
                right = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginX,
                bottom = TouchControllerSettingsManager.MAX_MARGINS.dp * settings.marginY
            ),
        primaryDial = primaryDial,
        secondaryDials = secondaryDials,
        primaryDialMaxSize = 160.dp * lerp(TouchControllerSettingsManager.MIN_SCALE, TouchControllerSettingsManager.MAX_SCALE, settings.scale),
        secondaryDialsBaseRotationInDegrees = -settings.rotation * TouchControllerSettingsManager.MAX_ROTATION
    )
}
