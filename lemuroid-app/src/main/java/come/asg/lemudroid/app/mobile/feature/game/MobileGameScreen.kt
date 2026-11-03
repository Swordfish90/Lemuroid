package come.asg.lemudroid.app.mobile.feature.game

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.LocalTouchControlEditor
import com.swordfish.touchinput.radial.settings.LocalTouchControllerSettings
import com.swordfish.touchinput.radial.settings.TouchControlEditorState
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.ControlSettings
import com.swordfish.touchinput.radial.settings.applyStickSensitivity
import com.swordfish.touchinput.radial.ui.GlassSurface
import com.swordfish.touchinput.radial.ui.LemuroidButtonPressFeedback
import come.asg.lemudroid.app.mobile.feature.settings.inputdevices.TouchLayoutEditorScreen
import come.asg.lemudroid.app.shared.game.BaseGameScreenViewModel
import come.asg.lemudroid.app.shared.game.viewmodel.GameViewModelTouchControls.Companion.MENU_LOADING_ANIMATION_MILLIS
import come.asg.lemudroid.app.shared.settings.HapticFeedbackMode
import come.asg.lemudroid.app.utils.android.settings.rememberTouchButtonAppearanceTheme
import come.asg.lemudroid.lib.preferences.SharedPreferencesHelper
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.ids.Id
import gg.padkit.inputevents.InputEvent
import gg.padkit.inputstate.InputState
import kotlin.math.roundToInt

@Composable
fun MobileGameScreen(viewModel: BaseGameScreenViewModel) {
    val appContext = LocalContext.current
    val viewportAlignment =
        remember(appContext) {
            SharedPreferencesHelper.getLegacySharedPreferences(appContext)
                .getString("viewport_alignment", "center") ?: "center"
        }
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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
        val editControls = viewModel.isEditControlShown().collectAsState(false)
        val editControllerId = viewModel.getTouchControllerId().collectAsState(null)
        var selectedControlKey by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(editControls.value) {
            if (!editControls.value) selectedControlKey = null
        }

        val touchControlEditor =
            TouchControlEditorState(
                isEditing = editControls.value,
                selectedControlKey = selectedControlKey,
                onSelect = { selectedControlKey = it },
                // Dragging is rendered locally by touchControlTransform. Commit once at the end
                // so pointer motion never waits on the settings StateFlow.
                onDrag = { _, _, _ -> },
                onDragEnd = { key, deltaX, deltaY ->
                    if (deltaX != 0f || deltaY != 0f) {
                        viewModel.updateControl(key) {
                            it.copy(
                                offsetX = (it.offsetX + deltaX).coerceIn(-MAX_OFFSET, MAX_OFFSET),
                                offsetY = (it.offsetY + deltaY).coerceIn(-MAX_OFFSET, MAX_OFFSET),
                            )
                        }
                    }
                    viewModel.persistTouchControls()
                },
            )

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

        val latestSettings = rememberUpdatedState(touchControllerSettings)
        val latestEditing = rememberUpdatedState(editControls.value)

        PadKit(
            modifier = Modifier.fillMaxSize(),
            onInputEvents = { events ->
                val controls = latestSettings.value?.controls.orEmpty()
                val hiddenKeys =
                    controls
                        .filter { (key, value) -> !value.visible && TouchControllerSettingsManager.isHideable(key) }
                        .keys
                // While editing, touches are for the editor only and must never reach the emulator.
                val deliverable =
                    if (latestEditing.value) {
                        emptyList()
                    } else {
                        events.filterNot { it.controlKey() in hiddenKeys }
                    }
                val adjustedEvents =
                    deliverable.map { event ->
                        if (event is InputEvent.ContinuousDirection && (event.id == 1 || event.id == 2)) {
                            val sensitivity =
                                controls[Id.ContinuousDirection(event.id).toString()]?.sensitivity
                                    ?: TouchControllerSettingsManager.DEFAULT_CONTROL_SENSITIVITY
                            event.copy(direction = applyStickSensitivity(event.direction, sensitivity))
                        } else {
                            event
                        }
                    }
                if (adjustedEvents.isNotEmpty()) {
                    viewModel.handleVirtualInputEvent(adjustedEvents)
                }
            },
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
                        touchControlsVisibleState.value &&
                        !editControls.value

                if (isVisible) {
                    CompositionLocalProvider(
                        LocalLemuroidPadTheme provides rememberTouchButtonAppearanceTheme(),
                        LocalTouchControllerSettings provides touchControllerSettings,
                        LocalTouchControlEditor provides touchControlEditor,
                    ) {
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

        if (editControls.value && editControllerId.value != null) {
            TouchLayoutEditorScreen(
                modifier = Modifier.fillMaxSize(),
                settingsManager = viewModel.getTouchControllerSettingsManager(),
                viewportAlignment = viewportAlignment,
                initialControllerId = editControllerId.value,
                allowTouchOverlay = { currentControllerConfig?.allowTouchOverlay ?: true },
                onClose = { viewModel.showEditControls(false) },
                onMenuPressed = {
                    viewModel.showEditControls(false)
                    viewModel.openMenu()
                },
            )
        }
    }
}

@Composable
internal fun PadContainer(modifier: Modifier = Modifier) {
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

@Composable
internal fun TouchControlEditorPanel(
    selectedControlKey: String?,
    settings: TouchControllerSettingsManager.Settings,
    onUpdateControl: (String, (ControlSettings) -> ControlSettings) -> Unit,
    onPersist: () -> Unit,
    onResetAll: () -> Unit,
    onDone: () -> Unit,
) {
    var confirmResetAll by remember { mutableStateOf(false) }
    var panelOffset by remember { mutableStateOf(Offset.Zero) }
    val selectedSettings =
        selectedControlKey?.let { key -> settings.controls[key] ?: ControlSettings() }
    val selectedName =
        TouchControllerSettingsManager.CONTROL_PRESETS
            .firstOrNull { it.key == selectedControlKey }
            ?.label
    val canHide = selectedControlKey?.let { TouchControllerSettingsManager.isHideable(it) } ?: true

    fun updateSelectedControl(update: (ControlSettings) -> ControlSettings) {
        val key = selectedControlKey ?: return
        onUpdateControl(key, update)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactLandscape = maxWidth > maxHeight
        Card(
            modifier =
                if (compactLandscape) {
                    Modifier.fillMaxWidth(0.58f).widthIn(max = 680.dp)
                        .offset { IntOffset(panelOffset.x.roundToInt(), panelOffset.y.roundToInt()) }
                        .padding(8.dp)
                } else {
                    Modifier.fillMaxWidth().widthIn(max = 760.dp)
                        .offset { IntOffset(panelOffset.x.roundToInt(), panelOffset.y.roundToInt()) }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                },
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth().padding(
                        horizontal = 12.dp,
                        vertical = if (compactLandscape) 5.dp else 8.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(if (compactLandscape) 2.dp else 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            selectedName?.let {
                                stringResource(come.asg.lemudroid.R.string.touch_control_editing, localizedTouchControlName(it))
                            } ?: stringResource(come.asg.lemudroid.R.string.touch_control_layout_editor),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier =
                            Modifier.weight(1f).pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    panelOffset += dragAmount
                                }
                            },
                    )
                    if (compactLandscape) {
                        Text(
                            stringResource(
                                if (canHide) come.asg.lemudroid.R.string.touch_control_visible else come.asg.lemudroid.R.string.touch_control_always_visible,
                            ),
                            fontSize = 11.sp,
                        )
                        Switch(
                            checked = selectedSettings?.visible ?: true,
                            enabled = selectedSettings != null && canHide,
                            onCheckedChange = { visible ->
                                updateSelectedControl { it.copy(visible = visible) }
                                onPersist()
                            },
                        )
                    }
                    IconButton(onClick = onPersist) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription =
                                stringResource(
                                    come.asg.lemudroid.R.string.touch_editor_save_layout,
                                ),
                        )
                    }
                }
                if (!compactLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            stringResource(
                                if (canHide) come.asg.lemudroid.R.string.touch_control_visible else come.asg.lemudroid.R.string.touch_control_always_visible,
                            ),
                            fontSize = 12.sp,
                        )
                        Switch(
                            checked = selectedSettings?.visible ?: true,
                            enabled = selectedSettings != null && canHide,
                            onCheckedChange = { visible ->
                                updateSelectedControl { it.copy(visible = visible) }
                                onPersist()
                            },
                        )
                    }
                }
                Text(stringResource(come.asg.lemudroid.R.string.touch_control_move_hint), fontSize = 10.sp)

                if (selectedSettings != null) {
                    if (compactLandscape) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(
                                        come.asg.lemudroid.R.string.touch_control_size,
                                        (selectedSettings.scale * 100).toInt(),
                                    ),
                                    fontSize = 11.sp,
                                )
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = selectedSettings.scale,
                                    onValueChange = { scale -> updateSelectedControl { it.copy(scale = scale) } },
                                    onValueChangeFinished = onPersist,
                                    valueRange =
                                        TouchControllerSettingsManager.MIN_CONTROL_SCALE..TouchControllerSettingsManager.MAX_CONTROL_SCALE,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(
                                        come.asg.lemudroid.R.string.touch_control_opacity,
                                        (selectedSettings.opacity * 100).toInt(),
                                    ),
                                    fontSize = 11.sp,
                                )
                                Slider(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = selectedSettings.opacity,
                                    onValueChange = { opacity -> updateSelectedControl { it.copy(opacity = opacity) } },
                                    onValueChangeFinished = onPersist,
                                    valueRange = TouchControllerSettingsManager.MIN_CONTROL_OPACITY..1f,
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                stringResource(
                                    come.asg.lemudroid.R.string.touch_control_size,
                                    (selectedSettings.scale * 100).toInt(),
                                ),
                                fontSize = 12.sp,
                            )
                            Slider(
                                modifier = Modifier.fillMaxWidth(),
                                value = selectedSettings.scale,
                                onValueChange = { scale -> updateSelectedControl { it.copy(scale = scale) } },
                                onValueChangeFinished = onPersist,
                                valueRange =
                                    TouchControllerSettingsManager.MIN_CONTROL_SCALE..TouchControllerSettingsManager.MAX_CONTROL_SCALE,
                            )
                        }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                stringResource(
                                    come.asg.lemudroid.R.string.touch_control_opacity,
                                    (selectedSettings.opacity * 100).toInt(),
                                ),
                                fontSize = 12.sp,
                            )
                            Slider(
                                modifier = Modifier.fillMaxWidth(),
                                value = selectedSettings.opacity,
                                onValueChange = { opacity -> updateSelectedControl { it.copy(opacity = opacity) } },
                                onValueChangeFinished = onPersist,
                                valueRange = TouchControllerSettingsManager.MIN_CONTROL_OPACITY..1f,
                            )
                        }
                    }
                    if (selectedControlKey != null && TouchControllerSettingsManager.isStick(selectedControlKey)) {
                        Text(
                            stringResource(
                                come.asg.lemudroid.R.string.touch_control_sensitivity,
                                (selectedSettings.sensitivity * 100).toInt(),
                            ),
                            fontSize = 11.sp,
                        )
                        Slider(
                            value = selectedSettings.sensitivity,
                            onValueChange = { sensitivity ->
                                updateSelectedControl { it.copy(sensitivity = sensitivity) }
                            },
                            onValueChangeFinished = onPersist,
                            valueRange =
                                TouchControllerSettingsManager.MIN_SENSITIVITY..TouchControllerSettingsManager.MAX_SENSITIVITY,
                            steps = 8,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            updateSelectedControl { ControlSettings() }
                            onPersist()
                        },
                        enabled = selectedSettings != null,
                    ) {
                        Text(stringResource(come.asg.lemudroid.R.string.touch_control_reset_control))
                    }
                    TextButton(onClick = {
                        confirmResetAll = true
                    }) { Text(stringResource(come.asg.lemudroid.R.string.touch_control_reset_all)) }
                    TextButton(
                        onClick = {
                            onPersist()
                            onDone()
                        },
                    ) {
                        Text(stringResource(R.string.touch_customize_button_done))
                    }
                }
            }
        }
    }

    if (confirmResetAll) {
        AlertDialog(
            onDismissRequest = { confirmResetAll = false },
            title = { Text(stringResource(come.asg.lemudroid.R.string.touch_control_reset_all_title)) },
            text = { Text(stringResource(come.asg.lemudroid.R.string.touch_control_reset_all_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAll()
                        confirmResetAll = false
                    },
                ) { Text(stringResource(come.asg.lemudroid.R.string.touch_control_reset_all)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmResetAll = false
                }) { Text(stringResource(come.asg.lemudroid.R.string.touch_editor_cancel)) }
            },
        )
    }
}

private const val MAX_OFFSET = TouchControllerSettingsManager.MAX_CONTROL_OFFSET

/** Maps a virtual-pad event to the settings key of the control that produced it. */
private fun InputEvent.controlKey(): String? =
    when (this) {
        is InputEvent.Button -> TouchControllerSettingsManager.controlKey(Id.Key(id))
        is InputEvent.DiscreteDirection -> TouchControllerSettingsManager.controlKey(Id.DiscreteDirection(id))
        is InputEvent.ContinuousDirection -> TouchControllerSettingsManager.controlKey(Id.ContinuousDirection(id))
        else -> null
    }

@Composable
private fun localizedTouchControlName(name: String): String =
    when (name) {
        "D-pad" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_dpad)
        "Left stick" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_left_stick)
        "Right stick" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_right_stick)
        "D-pad + left stick" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_dpad_left_stick)
        "Right D-pad" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_right_dpad)
        "Start" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_start)
        "Select" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_select)
        "Menu" -> stringResource(come.asg.lemudroid.R.string.touch_control_name_menu)
        else -> name // Letter/shoulder abbreviations are controller symbols, not prose.
    }
