package com.swordfish.lemuroid.app.mobile.feature.game

import android.graphics.RectF
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.GlassSurface
import com.swordfish.touchinput.radial.ui.LemuroidButtonPressFeedback
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.inputstate.InputState
import kotlin.math.roundToInt

@Composable
fun MobileGameScreen(viewModel: BaseGameScreenViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = constraints.maxWidth > constraints.maxHeight

        // Near-square portrait displays (foldable inner screens, tablets) with
        // dual-screen systems: let the stacked frame fill the whole screen and
        // overlay the pads on the lower half, 3DS-console style.
        val usePortraitOverlay =
            !isLandscape &&
                viewModel.isTouchScreenSystem &&
                constraints.maxHeight.toFloat() / constraints.maxWidth <= 1.5f

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
        val editControlsShown = viewModel.isEditControlShown().collectAsState(false)
        val screenPxWidth = constraints.maxWidth
        val screenPxHeight = constraints.maxHeight
        val touchControllerSettingsState =
            viewModel
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

        PadKit(
            modifier = Modifier.fillMaxSize(),
            onInputEvents = { viewModel.handleVirtualInputEvent(it) },
            hapticFeedbackType = padHapticFeedback,
            simulatedState = tiltSimulatedStates,
            simulatedControlIds = tiltSimulatedControls,
        ) {
            val localContext = LocalContext.current
            val lifecycle = LocalLifecycleOwner.current

            val fullScreenPosition = remember { mutableStateOf<Rect?>(null) }
            val viewportPosition = remember { mutableStateOf<Rect?>(null) }

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

            LaunchedEffect(fullPos, viewPos) {
                val gameView = viewModel.retroGameView.retroGameViewFlow()
                if (fullPos == null || viewPos == null) return@LaunchedEffect
                val viewport =
                    RectF(
                        (viewPos.left - fullPos.left) / fullPos.width,
                        (viewPos.top - fullPos.top) / fullPos.height,
                        (viewPos.right - fullPos.left) / fullPos.width,
                        (viewPos.bottom - fullPos.top) / fullPos.height,
                    )
                gameView.viewport = viewport
            }

            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
                constraintSet =
                    GameScreenLayout.buildConstraintSet(
                        isLandscape,
                        currentControllerConfig?.allowTouchOverlay ?: true,
                        usePortraitOverlay,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                            .onGloballyPositioned { viewportPosition.value = it.boundsInRoot() },
                )

                val isVisible =
                    touchControllerSettings != null &&
                        currentControllerConfig != null &&
                        touchControlsVisibleState.value

                if (isVisible) {
                    CompositionLocalProvider(LocalLemuroidPadTheme provides LemuroidPadTheme()) {
                        if (!isLandscape && !usePortraitOverlay) {
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
                            Modifier
                                .layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD)
                                .offset {
                                    val s = touchControllerSettingsState.value ?: return@offset IntOffset.Zero
                                    IntOffset(
                                        (screenPxWidth * s.leftOffsetX).roundToInt(),
                                        (screenPxHeight * s.leftOffsetY).roundToInt(),
                                    )
                                }
                                .alpha(if (editControlsShown.value) 0.7f else 1f)
                                .movePadWhileEditing(editControlsShown.value) { dx, dy ->
                                    val s = touchControllerSettingsState.value ?: return@movePadWhileEditing
                                    viewModel.moveTouchControllerSettings(
                                        s,
                                        isLeft = true,
                                        dxFraction = dx / screenPxWidth,
                                        dyFraction = dy / screenPxHeight,
                                    )
                                },
                            touchControllerSettings,
                        )
                        rightGamePad?.invoke(
                            this,
                            Modifier
                                .layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD)
                                .offset {
                                    val s = touchControllerSettingsState.value ?: return@offset IntOffset.Zero
                                    IntOffset(
                                        (screenPxWidth * s.rightOffsetX).roundToInt(),
                                        (screenPxHeight * s.rightOffsetY).roundToInt(),
                                    )
                                }
                                .alpha(if (editControlsShown.value) 0.7f else 1f)
                                .movePadWhileEditing(editControlsShown.value) { dx, dy ->
                                    val s = touchControllerSettingsState.value ?: return@movePadWhileEditing
                                    viewModel.moveTouchControllerSettings(
                                        s,
                                        isLeft = false,
                                        dxFraction = dx / screenPxWidth,
                                        dyFraction = dy / screenPxHeight,
                                    )
                                },
                            touchControllerSettings,
                        )

                        GameScreenRunningCentralMenu(
                            modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER),
                            viewModel = viewModel,
                        )
                    }
                }
            }

            // Non-modal edit panel: while visible, pad groups can be dragged
            // anywhere on screen (offsets persist per system + orientation).
            if (editControlsShown.value && touchControllerSettings != null && currentControllerConfig != null) {
                TouchControlsEditPanel(
                    viewModel = viewModel,
                    controllerConfig = currentControllerConfig,
                    touchControllerSettings = touchControllerSettings,
                )
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

@Composable
private fun GameScreenRunningCentralMenu(
    modifier: Modifier = Modifier,
    viewModel: BaseGameScreenViewModel,
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
    }
}

// Captures all pointer events in the Initial pass while editing, so an entire
// pad group behaves as a single draggable surface (child buttons stay idle).
private fun Modifier.movePadWhileEditing(
    editing: Boolean,
    onDelta: (Float, Float) -> Unit,
): Modifier =
    pointerInput(editing) {
        if (!editing) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            down.consume()
            var last = down.position
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val delta = change.position - last
                last = change.position
                if (delta != Offset.Zero) onDelta(delta.x, delta.y)
                change.consume()
                if (event.changes.all { !it.pressed }) break
            }
        }
    }

@Composable
private fun TouchControlsEditPanel(
    viewModel: BaseGameScreenViewModel,
    controllerConfig: ControllerConfig,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
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
                Text(
                    text = "Drag a pad group to move it anywhere on screen",
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
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
