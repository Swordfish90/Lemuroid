@file:OptIn(ExperimentalLayoutApi::class)

package com.swordfish.lemuroid.app.shared.game

import android.view.KeyEvent
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.LemuroidPadTheme
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.LemuroidButtonPressFeedback
import gg.jam.jampadcompose.JamPad
import gg.jam.jampadcompose.config.HapticFeedbackType
import gg.jam.jampadcompose.inputevents.InputEvent
import gg.jam.jampadcompose.inputstate.InputState

private const val CONSTRAINTS_LEFT_PAD = "leftPad"
private const val CONSTRAINTS_RIGHT_PAD = "rightPad"
private const val CONSTRAINTS_GAME_VIEW = "gameView"
private const val CONSTRAINTS_GAME_CONTAINER = "gameContainer"

@Composable
fun GameScreen(
    viewModel: GameScreenViewModel,
    onVirtualGamePadInputEvents: (List<InputEvent>) -> Unit,
    buildRetroView: (GameLoader.GameData, GLRetroViewData) -> View,
) {
    val uiState = viewModel.getUiState().collectAsState(GameScreenViewModel.UiState.Loading("")).value
    when (uiState) {
        is GameScreenViewModel.UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is GameScreenViewModel.UiState.Running -> {
            GameScreenRunning(
                viewModel,
                buildRetroView,
                onVirtualGamePadInputEvents,
                uiState,
            )
        }

        else -> {}
    }
}

@Composable
private fun GameScreenRunning(
    viewModel: GameScreenViewModel,
    buildRetroView: (GameLoader.GameData, GLRetroViewData) -> View,
    onVirtualGamePadInputEvents: (List<InputEvent>) -> Unit,
    state: GameScreenViewModel.UiState.Running,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = constraints.maxWidth > constraints.maxHeight

        LaunchedEffect(isLandscape) {
            val orientation = if (isLandscape) {
                TouchControllerSettingsManager.Orientation.LANDSCAPE
            } else {
                TouchControllerSettingsManager.Orientation.PORTRAIT
            }
            viewModel.onScreenOrientationChanged(orientation)
        }

        val controllerConfigState = viewModel.getTouchControllerConfig().collectAsState(null)
        val touchControlsVisibleState = viewModel.isTouchControllerVisible().collectAsState(false)
        val touchControllerSettingsState = viewModel
            .getTouchControlsSettings(LocalDensity.current, WindowInsets.displayCutout)
            .collectAsState(null)

        val touchControllerSettings = touchControllerSettingsState.value
        val currentControllerConfig = controllerConfigState.value

        val tiltConfiguration = viewModel.getTiltConfiguration().collectAsState(TiltConfiguration.Disabled)
        val tiltSimulatedStates = viewModel.getSimulatedTiltEvents().collectAsState(InputState())
        val tiltSimulatedControls = remember { derivedStateOf { tiltConfiguration.value.controlIds() } }

        val touchGamePads = currentControllerConfig?.getTouchControllerConfig()
        val leftGamePad = touchGamePads?.leftComposable
        val rightGamePad = touchGamePads?.rightComposable

        val padHapticFeedback = when (state.hapticFeedbackMode) {
            HapticFeedbackMode.NONE -> HapticFeedbackType.NONE
            HapticFeedbackMode.PRESS -> HapticFeedbackType.PRESS
            HapticFeedbackMode.PRESS_RELEASE -> HapticFeedbackType.PRESS_RELEASE
        }

        JamPad(
            modifier = Modifier.fillMaxSize(),
            onInputEvents = { events ->
                val menuEvent = events.firstOrNull { it is InputEvent.Button && it.id == KeyEvent.KEYCODE_BUTTON_MODE }
                if (menuEvent != null) {
                    viewModel.onMenuPressed((menuEvent as InputEvent.Button).pressed)
                }
                onVirtualGamePadInputEvents(events)
            },
            hapticFeedbackType = padHapticFeedback,
            simulatedState = tiltSimulatedStates,
            simulatedControlIds = tiltSimulatedControls
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
                constraintSet = buildConstraintSet(
                    isLandscape,
                    currentControllerConfig?.allowTouchOverlay ?: true
                )
            ) {
                AndroidView(
                    modifier = Modifier
                        .layoutId(CONSTRAINTS_GAME_VIEW)
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top)),
                    factory = { buildRetroView(state.gameData, state.gameViewData) }
                )

                if (touchControllerSettings != null && currentControllerConfig != null && touchControlsVisibleState.value) {
                    CompositionLocalProvider(LocalLemuroidPadTheme provides LemuroidPadTheme(MaterialTheme.colorScheme)) {
                        leftGamePad?.invoke(
                            this,
                            Modifier.layoutId(CONSTRAINTS_LEFT_PAD),
                            touchControllerSettings,
                        )
                        rightGamePad?.invoke(
                            this,
                            Modifier.layoutId(CONSTRAINTS_RIGHT_PAD),
                            touchControllerSettings
                        )

                        GameScreenRunningCentralMenu(
                            modifier = Modifier.layoutId(CONSTRAINTS_GAME_CONTAINER),
                            controllerConfig = currentControllerConfig,
                            touchControllerSettings = touchControllerSettings,
                            viewModel = viewModel,
                            state = state
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameScreenRunningCentralMenu(
    modifier: Modifier = Modifier,
    viewModel: GameScreenViewModel,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    controllerConfig: ControllerConfig,
    state: GameScreenViewModel.UiState.Running,
) {
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        LemuroidButtonPressFeedback(
            pressed = state.menuPressed,
            animationDurationMillis = GameScreenViewModel.MENU_LOADING_ANIMATION_MILLIS,
            icon = R.drawable.button_menu,
        )
        MenuEditTouchControls(viewModel, state, controllerConfig, touchControllerSettings)
    }
}

@Composable
private fun MenuEditTouchControls(
    viewModel: GameScreenViewModel,
    state: GameScreenViewModel.UiState.Running,
    controllerConfig: ControllerConfig,
    touchControllerSettings: TouchControllerSettingsManager.Settings
) {
    if (!state.showEditControls) return

    Dialog(onDismissRequest = { viewModel.showEditControls(false) }) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuEditTouchControlRow(Icons.Default.OpenInFull, "Scale", 0f) {
                    Slider(
                        value = touchControllerSettings.scale,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(scale = it)
                            )
                        }
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Horizontal Margin", 90f) {
                    Slider(
                        value = touchControllerSettings.marginX,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginX = it)
                            )
                        }
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Vertical Margin", 0f) {
                    Slider(
                        value = touchControllerSettings.marginY,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginY = it)
                            )
                        }
                    )
                }
                if (controllerConfig.allowTouchRotation) {
                    MenuEditTouchControlRow(Icons.Default.RotateLeft, "Rotate", 0f) {
                        Slider(
                            value = touchControllerSettings.rotation,
                            onValueChange = {
                                viewModel.updateTouchControllerSettings(
                                    touchControllerSettings.copy(rotation = it)
                                )
                            }
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
    slider: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.rotate(rotation),
            imageVector = icon,
            contentDescription = label
        )
        slider()
    }
}

private fun buildConstraintSet(isLandscape: Boolean, allowTouchOverlay: Boolean): ConstraintSet {
    return if (isLandscape) {
        buildConstraintSetLandscape(allowTouchOverlay)
    } else {
        buildConstraintSetPortrait()
    }
}

private fun buildConstraintSetPortrait(): ConstraintSet {
    return ConstraintSet {
        val gameView = createRefFor(CONSTRAINTS_GAME_VIEW)
        val leftPad = createRefFor(CONSTRAINTS_LEFT_PAD)
        val rightPad = createRefFor(CONSTRAINTS_RIGHT_PAD)
        val gameContainer = createRefFor(CONSTRAINTS_GAME_CONTAINER)

        val gamePadChain = createHorizontalChain(leftPad, rightPad, chainStyle = ChainStyle.SpreadInside)

        constrain(gameView) {
            height = Dimension.fillToConstraints
            top.linkTo(parent.top)
            absoluteLeft.linkTo(parent.absoluteLeft)
            absoluteRight.linkTo(parent.absoluteRight)
            bottom.linkTo(leftPad.top)
        }

        constrain(gamePadChain) {
            absoluteLeft.linkTo(parent.absoluteLeft)
            absoluteRight.linkTo(parent.absoluteRight)
        }

        constrain(rightPad) {
            width = Dimension.fillToConstraints
            bottom.linkTo(parent.bottom)
        }

        constrain(leftPad) {
            width = Dimension.fillToConstraints
            bottom.linkTo(parent.bottom)
        }

        constrain(gameContainer) {
            absoluteLeft.linkTo(gameView.absoluteLeft)
            absoluteRight.linkTo(gameView.absoluteRight)
            top.linkTo(gameView.top)
            bottom.linkTo(gameView.bottom)
        }
    }
}

private fun buildConstraintSetLandscape(allowTouchOverlay: Boolean): ConstraintSet {
    return ConstraintSet {
        val gameView = createRefFor(CONSTRAINTS_GAME_VIEW)
        val leftPad = createRefFor(CONSTRAINTS_LEFT_PAD)
        val rightPad = createRefFor(CONSTRAINTS_RIGHT_PAD)
        val gameContainer = createRefFor(CONSTRAINTS_GAME_CONTAINER)

        val gamePadChain = createHorizontalChain(leftPad, rightPad, chainStyle = ChainStyle.SpreadInside)

        constrain(gameView) {
            width = Dimension.fillToConstraints
            top.linkTo(parent.top)
            if (allowTouchOverlay) {
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            } else {
                absoluteLeft.linkTo(leftPad.absoluteRight)
                absoluteRight.linkTo(rightPad.absoluteLeft)
            }
            bottom.linkTo(parent.bottom)
        }

        constrain(gamePadChain) {
            absoluteLeft.linkTo(parent.absoluteLeft)
            absoluteRight.linkTo(parent.absoluteRight)
        }

        constrain(rightPad) {
            verticalBias = 1.0f
            bottom.linkTo(parent.bottom)
            top.linkTo(parent.top)
        }

        constrain(leftPad) {
            verticalBias = 1.0f
            bottom.linkTo(parent.bottom)
            top.linkTo(parent.top)
        }

        constrain(gameContainer) {
            absoluteLeft.linkTo(gameView.absoluteLeft)
            absoluteRight.linkTo(gameView.absoluteRight)
            top.linkTo(gameView.top)
            bottom.linkTo(gameView.bottom)
        }
    }
}
