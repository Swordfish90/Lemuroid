@file:OptIn(ExperimentalLayoutApi::class)

package com.swordfish.lemuroid.app.shared.game

import android.view.KeyEvent
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.layouts.ComposeTouchLayouts.MOTION_SOURCE_DPAD
import com.swordfish.touchinput.radial.layouts.LemuroidButtonBackground
import com.swordfish.touchinput.radial.layouts.LemuroidButtonForeground
import com.swordfish.touchinput.radial.layouts.PSXDualShockLeft
import com.swordfish.touchinput.radial.layouts.PSXDualShockRight
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.JamPad
import gg.jam.jampadcompose.ids.DiscreteDirectionId
import gg.jam.jampadcompose.inputevents.InputEvent
import gg.jam.jampadcompose.inputstate.InputState
import kotlinx.coroutines.delay
import timber.log.Timber

private const val CONSTRAINTS_LEFT_PAD = "leftPad"
private const val CONSTRAINTS_RIGHT_PAD = "rightPad"
private const val CONSTRAINTS_GAME_VIEW = "gameView"
private const val CONSTRAINTS_GAME_CONTAINER = "gameContainer"

@Composable
fun GameScreen(
    viewModel: GameScreenViewModel,
    onVirtualGamePadInputEvents: (List<InputEvent>) -> Unit,
    gamePadConfig: ControllerConfig?,
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
                gamePadConfig?.allowTouchOverlay ?: true,
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
    allowTouchOverlay: Boolean,
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

        val isTouchControlsVisible = viewModel.isTouchControllerVisible().collectAsState(false)

        val touchControllerSettings = viewModel
            .getTouchControlsSettings(
                LocalDensity.current,
                WindowInsets.displayCutout
            )
            .collectAsState(null)
            .value

        val tiltConfiguration = viewModel.getTiltConfiguration().collectAsState(TiltConfiguration.Disabled)
        val tiltSimulatedStates = viewModel.getSimulatedTiltEvents().collectAsState(InputState())
        val tiltSimulatedControls = remember { derivedStateOf { tiltConfiguration.value.controlIds() } }

        JamPad(
            modifier = Modifier.fillMaxSize(),
            onInputEvents = { events ->
                val menuEvent = events.firstOrNull { it is InputEvent.Button && it.id == KeyEvent.KEYCODE_BUTTON_MODE }
                if (menuEvent != null) {
                    viewModel.onMenuPressed((menuEvent as InputEvent.Button).pressed)
                }
                onVirtualGamePadInputEvents(events)
            },
            simulatedState = tiltSimulatedStates,
            simulatedControlIds = tiltSimulatedControls
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
                constraintSet = buildConstraintSet(isLandscape, allowTouchOverlay)
            ) {
                AndroidView(
                    modifier = Modifier.layoutId(CONSTRAINTS_GAME_VIEW)
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top)),
                    factory = { buildRetroView(state.gameData, state.gameViewData) }
                )

                if (touchControllerSettings != null && isTouchControlsVisible.value) {
                    PSXDualShockLeft(
                        modifier = Modifier.layoutId(CONSTRAINTS_LEFT_PAD),
                        settings = touchControllerSettings
                    )
                    PSXDualShockRight(
                        modifier = Modifier.layoutId(CONSTRAINTS_RIGHT_PAD),
                        settings = touchControllerSettings
                    )

                    GameScreenRunningCentralMenu(
                        modifier = Modifier.layoutId(CONSTRAINTS_GAME_CONTAINER),
                        touchControllerSettings = touchControllerSettings,
                        viewModel = viewModel,
                        state = state
                    )
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
    state: GameScreenViewModel.UiState.Running,
) {
    Box(
        modifier = modifier
            .width(200.dp)
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        MenuPressedFeedback(state)
        MenuEditTouchControls(viewModel, state, touchControllerSettings)
    }
}

@Composable
private fun MenuPressedFeedback(state: GameScreenViewModel.UiState.Running) {
    Box(modifier = Modifier.size(64.dp)) {
        var shouldShow by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = GameScreenViewModel.MENU_LOADING_ANIMATION_MILLIS,
                easing = LinearEasing
            ),
            label = "progress",
            finishedListener = {
                if (progress > 0.5) {
                    shouldShow = false
                }
            }
        )

        LaunchedEffect(state.menuPressed) {
            if (state.menuPressed) {
                shouldShow = true
                progress = 1f
            } else {
                progress = 0f
                delay(GameScreenViewModel.MENU_LOADING_ANIMATION_MILLIS.toLong())
                shouldShow = false
            }
        }

        AnimatedVisibility(shouldShow, enter = fadeIn(), exit = fadeOut()) {
            LemuroidButtonBackground()
            LemuroidButtonForeground(pressed = false, icon = R.drawable.button_menu)
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = { animatedProgress }
            )
        }
    }
}

@Composable
private fun MenuEditTouchControls(
    viewModel: GameScreenViewModel,
    state: GameScreenViewModel.UiState.Running,
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
