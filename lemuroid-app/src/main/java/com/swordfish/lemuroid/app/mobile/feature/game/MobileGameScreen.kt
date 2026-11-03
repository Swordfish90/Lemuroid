package com.swordfish.lemuroid.app.mobile.feature.game

import android.graphics.RectF
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.swordfish.lemuroid.app.shared.game.BaseGameScreenViewModel
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTouchControls.Companion.MENU_LOADING_ANIMATION_MILLIS
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.LemuroidPadTheme
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.GameScreenSettingsManager
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.GlassSurface
import com.swordfish.touchinput.radial.ui.LemuroidButtonPressFeedback
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.inputstate.InputState

@Composable
fun MobileGameScreen(viewModel: BaseGameScreenViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = constraints.maxWidth > constraints.maxHeight

        LaunchedEffect(isLandscape) {
            val orientation =
                if (isLandscape) {
                    TouchControllerSettingsManager.Orientation.LANDSCAPE
                } else {
                    TouchControllerSettingsManager.Orientation.PORTRAIT
                }
            viewModel.onScreenOrientationChanged(orientation)
        }

        val controllerConfigState = viewModel.getTouchControllerConfig().collectAsState(null)
        val touchControlsVisibleState = viewModel.isTouchControllerVisible().collectAsState(false)
        val touchControllerSettingsState =
            viewModel
                .getTouchControlsSettings(LocalDensity.current, WindowInsets.displayCutout)
                .collectAsState(null)

        val touchControllerSettings = touchControllerSettingsState.value
        val currentControllerConfig = controllerConfigState.value

        val gameScreenSettings =
            viewModel
                .getGameScreenSettings()
                .collectAsState(GameScreenSettingsManager.Settings())
                .value

        val tiltConfiguration = viewModel.getTiltConfiguration().collectAsState(TiltConfiguration.Disabled)
        val tiltSimulatedStates = viewModel.getSimulatedTiltEvents().collectAsState(InputState())
        val tiltSimulatedControls = remember { derivedStateOf { tiltConfiguration.value.controlIds() } }

        val touchGamePads = currentControllerConfig?.getTouchControllerConfig()
        val leftGamePad = touchGamePads?.leftComposable
        val rightGamePad = touchGamePads?.rightComposable

        val hapticFeedbackMode =
            viewModel
                .getTouchHapticFeedbackMode()
                .collectAsState(HapticFeedbackMode.NONE)

        val padHapticFeedback =
            when (hapticFeedbackMode.value) {
                HapticFeedbackMode.NONE -> HapticFeedbackType.NONE
                HapticFeedbackMode.PRESS -> HapticFeedbackType.PRESS
                HapticFeedbackMode.PRESS_RELEASE -> HapticFeedbackType.PRESS_RELEASE
            }

        val fullScreenPosition = remember { mutableStateOf<Rect?>(null) }
        val viewportPosition = remember { mutableStateOf<Rect?>(null) }

        val isVisible =
            touchControllerSettings != null &&
                currentControllerConfig != null &&
                touchControlsVisibleState.value

        // When the touch controls are hidden (e.g. a physical controller is connected), keep the game
        // screen where it would be with the controls shown, instead of letting it expand.
        val measureWithHiddenPads =
            !isVisible &&
                touchControllerSettings != null &&
                currentControllerConfig != null

        if (measureWithHiddenPads) {
            HiddenPadsViewportMeasurer(
                isLandscape = isLandscape,
                controllerConfig = currentControllerConfig,
                touchControllerSettings = touchControllerSettings,
                onViewportPositioned = { viewportPosition.value = it },
            )
        }

        PadKit(
            modifier = Modifier.fillMaxSize(),
            onInputEvents = { viewModel.handleVirtualInputEvent(it) },
            hapticFeedbackType = padHapticFeedback,
            simulatedState = tiltSimulatedStates,
            simulatedControlIds = tiltSimulatedControls,
        ) {
            val localContext = LocalContext.current
            val lifecycle = LocalLifecycleOwner.current

            AndroidView(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { fullScreenPosition.value = it.boundsInRoot() },
                factory = {
                    viewModel.createRetroView(localContext, lifecycle)
                },
            )

            val fullPos = fullScreenPosition.value
            val viewPos = viewportPosition.value

            LaunchedEffect(fullPos, viewPos, gameScreenSettings) {
                val gameView = viewModel.retroGameView.retroGameViewFlow()
                if (fullPos == null || viewPos == null) return@LaunchedEffect
                val baseViewport =
                    RectF(
                        (viewPos.left - fullPos.left) / fullPos.width,
                        (viewPos.top - fullPos.top) / fullPos.height,
                        (viewPos.right - fullPos.left) / fullPos.width,
                        (viewPos.bottom - fullPos.top) / fullPos.height,
                    )
                gameView.viewport = applyScreenSettings(baseViewport, gameScreenSettings)
            }

            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
                constraintSet =
                    GameScreenLayout.buildConstraintSet(
                        isLandscape,
                        currentControllerConfig?.allowTouchOverlay ?: true,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                            .onGloballyPositioned {
                                if (!measureWithHiddenPads) viewportPosition.value = it.boundsInRoot()
                            },
                )

                if (isVisible) {
                    CompositionLocalProvider(LocalLemuroidPadTheme provides LemuroidPadTheme()) {
                        if (!isLandscape) {
                            PadContainer(
                                modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER),
                            )
                        } else if (!currentControllerConfig.allowTouchOverlay) {
                            PadContainer(
                                modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_CONTAINER),
                            )
                            PadContainer(
                                modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_CONTAINER),
                            )
                        }

                        leftGamePad?.invoke(
                            this,
                            Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD),
                            touchControllerSettings,
                        )
                        rightGamePad?.invoke(
                            this,
                            Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD),
                            touchControllerSettings,
                        )

                        GameScreenRunningCentralMenu(
                            modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER),
                            controllerConfig = currentControllerConfig,
                            touchControllerSettings = touchControllerSettings,
                            viewModel = viewModel,
                        )
                    }
                }
            }
        }

        val isLoading =
            viewModel.loadingState
                .collectAsState(true)
                .value

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        ScreenAdjustOverlay(viewModel = viewModel, settings = gameScreenSettings)
    }
}

@Composable
private fun PadContainer(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current
    GlassSurface(
        modifier = modifier,
        cornerRadius = theme.level0CornerRadius,
        fillColor = theme.level0Fill,
        shadowColor = theme.level0Shadow,
        shadowWidth = theme.level0ShadowWidth,
    )
}

/**
 * Lays out the touch pads invisibly (with a separate, inert PadKit) only to measure where the game
 * view would be if they were shown. Used to keep the game screen in place when the touch controls
 * are hidden, for instance when a physical controller is connected.
 */
@Composable
private fun HiddenPadsViewportMeasurer(
    isLandscape: Boolean,
    controllerConfig: ControllerConfig,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    onViewportPositioned: (Rect) -> Unit,
) {
    val touchGamePads = controllerConfig.getTouchControllerConfig()
    PadKit(
        modifier = Modifier.fillMaxSize().alpha(0f),
        onInputEvents = { },
        hapticFeedbackType = HapticFeedbackType.NONE,
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize(),
            constraintSet = GameScreenLayout.buildConstraintSet(isLandscape, controllerConfig.allowTouchOverlay),
        ) {
            Box(
                modifier =
                    Modifier
                        .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                        .onGloballyPositioned { onViewportPositioned(it.boundsInRoot()) },
            )
            CompositionLocalProvider(LocalLemuroidPadTheme provides LemuroidPadTheme()) {
                touchGamePads.leftComposable(
                    this@PadKit,
                    Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD),
                    touchControllerSettings,
                )
                touchGamePads.rightComposable(
                    this@PadKit,
                    Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD),
                    touchControllerSettings,
                )
            }
        }
    }
}

@Composable
private fun GameScreenRunningCentralMenu(
    modifier: Modifier = Modifier,
    viewModel: BaseGameScreenViewModel,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    controllerConfig: ControllerConfig,
) {
    val menuPressed = viewModel.isMenuPressed().collectAsState(false)
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        LemuroidButtonPressFeedback(
            pressed = menuPressed.value,
            animationDurationMillis = MENU_LOADING_ANIMATION_MILLIS,
            icon = R.drawable.button_menu,
        )
        MenuEditTouchControls(viewModel, controllerConfig, touchControllerSettings)
    }
}

@Composable
private fun MenuEditTouchControls(
    viewModel: BaseGameScreenViewModel,
    controllerConfig: ControllerConfig,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
) {
    val showEditControls = viewModel.isEditControlShown().collectAsState(false)
    if (!showEditControls.value) return

    Dialog(onDismissRequest = { viewModel.showEditControls(false) }) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MenuEditTouchControlRow(Icons.Default.OpenInFull, "Scale", 0f) {
                    Slider(
                        value = touchControllerSettings.scale,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(scale = it),
                            )
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Horizontal Margin", 90f) {
                    Slider(
                        value = touchControllerSettings.marginX,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginX = it),
                            )
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Vertical Margin", 0f) {
                    Slider(
                        value = touchControllerSettings.marginY,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginY = it),
                            )
                        },
                    )
                }
                if (controllerConfig.allowTouchRotation) {
                    MenuEditTouchControlRow(Icons.Default.RotateLeft, "Rotate", 0f) {
                        Slider(
                            value = touchControllerSettings.rotation,
                            onValueChange = {
                                viewModel.updateTouchControllerSettings(
                                    touchControllerSettings.copy(rotation = it),
                                )
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { viewModel.resetTouchControls() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_reset))
                    }
                    TextButton(
                        onClick = { viewModel.showEditControls(false) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_done))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuEditTouchControlRow(
    icon: ImageVector,
    label: String,
    rotation: Float,
    slider: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.rotate(rotation),
            imageVector = icon,
            contentDescription = label,
        )
        slider()
    }
}

@Composable
private fun ScreenAdjustOverlay(
    viewModel: BaseGameScreenViewModel,
    settings: GameScreenSettingsManager.Settings,
) {
    val showScreenAdjust = viewModel.isScreenAdjustShown().collectAsState(false)
    if (!showScreenAdjust.value) return

    Dialog(onDismissRequest = { viewModel.showScreenAdjust(false) }) {
        // Keep the game fully visible behind the dialog while adjusting it.
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MenuEditTouchControlRow(Icons.Default.OpenInFull, "Size", 0f) {
                    Slider(
                        value = settings.scale,
                        valueRange = GameScreenSettingsManager.MIN_SCALE..GameScreenSettingsManager.MAX_SCALE,
                        onValueChange = {
                            viewModel.updateGameScreenSettings(settings.copy(scale = it))
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Horizontal", 90f) {
                    Slider(
                        value = settings.offsetX,
                        valueRange = -1f..1f,
                        onValueChange = {
                            viewModel.updateGameScreenSettings(settings.copy(offsetX = it))
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Vertical", 0f) {
                    Slider(
                        value = settings.offsetY,
                        valueRange = -1f..1f,
                        onValueChange = {
                            viewModel.updateGameScreenSettings(settings.copy(offsetY = it))
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { viewModel.resetGameScreenSettings() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_reset))
                    }
                    TextButton(
                        onClick = { viewModel.showScreenAdjust(false) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_done))
                    }
                }
            }
        }
    }
}

private const val MIN_VIEWPORT_CENTER_X = -0.5f
private const val MAX_VIEWPORT_CENTER_X = 1.5f

// Fraction of the scaled height still available to the image at the end of the vertical slider.
private const val MIN_VERTICAL_SQUEEZE = 0.3f

private fun applyScreenSettings(
    base: RectF,
    settings: GameScreenSettingsManager.Settings,
): RectF {
    val width = base.width() * settings.scale
    val height = base.height() * settings.scale

    // Horizontally the image may extend past the screen edges (LibretroDroid does not clip it).
    val centerX =
        (base.centerX() + settings.offsetX * GameScreenSettingsManager.MAX_OFFSET)
            .coerceIn(MIN_VIEWPORT_CENTER_X, MAX_VIEWPORT_CENTER_X)

    // Vertically the viewport never leaves the base area (between the top of the screen and the
    // touch controls), and LibretroDroid always fits the image inside the viewport. The image is
    // therefore at most pushed against the top of the screen or the controls: past that point the
    // viewport gets clipped and the image shrinks against the edge. Slider up moves the image up.
    val maxTravel = (base.height() - height) / 2f + height * (1f - MIN_VERTICAL_SQUEEZE)
    val centerY = base.centerY() - settings.offsetY * maxTravel
    val top = (centerY - height / 2f).coerceIn(base.top, base.bottom)
    val bottom = (centerY + height / 2f).coerceIn(base.top, base.bottom)

    return RectF(
        centerX - width / 2f,
        top,
        centerX + width / 2f,
        bottom,
    )
}
