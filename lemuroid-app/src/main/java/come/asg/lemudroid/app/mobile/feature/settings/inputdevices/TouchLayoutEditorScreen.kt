package come.asg.lemudroid.app.mobile.feature.settings.inputdevices

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.settings.LocalTouchControlEditor
import com.swordfish.touchinput.radial.settings.LocalTouchControllerSettings
import com.swordfish.touchinput.radial.settings.TouchControlEditorState
import com.swordfish.touchinput.radial.settings.TouchControllerID
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import come.asg.lemudroid.app.mobile.feature.game.GameScreenLayout
import come.asg.lemudroid.app.mobile.feature.game.PadContainer
import come.asg.lemudroid.app.mobile.feature.game.TouchControlEditorPanel
import come.asg.lemudroid.app.utils.android.settings.rememberTouchButtonAppearanceTheme
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.inputevents.InputEvent
import kotlinx.coroutines.launch

/**
 * @param allowTouchOverlay whether the given console draws its pads over the game in landscape. The game
 * takes this from its ControllerConfig; wire the same lookup here so landscape matches in-game for
 * consoles that use side containers. Defaults to overlay.
 */
@Composable
fun TouchLayoutEditorScreen(
    modifier: Modifier = Modifier,
    settingsManager: TouchControllerSettingsManager,
    viewportAlignment: String = "center",
    allowTouchOverlay: (TouchControllerID) -> Boolean = { true },
    initialControllerId: TouchControllerID? = null,
    onClose: (() -> Unit)? = null,
    onMenuPressed: (() -> Unit)? = null,
) {
    // Same orientation rule as the game (width > height), so both always pick the same layout bucket,
    // including split-screen and foldables.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        TouchLayoutEditorContent(
            isLandscape = constraints.maxWidth > constraints.maxHeight,
            settingsManager = settingsManager,
            viewportAlignment = viewportAlignment,
            allowTouchOverlay = allowTouchOverlay,
            initialControllerId = initialControllerId,
            onClose = onClose,
            onMenuPressed = onMenuPressed,
        )
    }
}

@Composable
private fun TouchLayoutEditorContent(
    isLandscape: Boolean,
    settingsManager: TouchControllerSettingsManager,
    viewportAlignment: String,
    allowTouchOverlay: (TouchControllerID) -> Boolean,
    initialControllerId: TouchControllerID?,
    onClose: (() -> Unit)?,
    onMenuPressed: (() -> Unit)?,
) {
    // Saveable: system rotation can recreate the activity while keeping the editor open.
    var controllerId by rememberSaveable { mutableStateOf(initialControllerId ?: TouchControllerID.GB) }
    var layoutMenuExpanded by remember { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val orientation =
        if (isLandscape) {
            TouchControllerSettingsManager.Orientation.LANDSCAPE
        } else {
            TouchControllerSettingsManager.Orientation.PORTRAIT
        }
    var selectedControlKey by rememberSaveable(controllerId, orientation) { mutableStateOf<String?>(null) }

    val safeDrawingInsets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    val insets = WindowInsets.displayCutout
    val settingsFlow =
        remember(controllerId, orientation, density, insets, settingsManager) {
            settingsManager.observeSettings(controllerId, orientation, density, insets)
        }
    // Null until the real settings arrive, so nothing renders (or gets saved) with zero margins.
    val settings = settingsFlow.collectAsState(null).value
    val coroutineScope = rememberCoroutineScope()
    val padConfig = remember(controllerId) { TouchControllerID.getConfig(controllerId) }

    fun updateControl(
        key: String,
        transform: (TouchControllerSettingsManager.ControlSettings) -> TouchControllerSettingsManager.ControlSettings,
    ) = settingsManager.updateControl(controllerId, orientation, key, transform)

    fun persist() = settingsManager.persist(controllerId, orientation)

    DisposableEffect(controllerId, orientation, settingsManager) {
        onDispose { settingsManager.persist(controllerId, orientation) }
    }

    val maxOffset = TouchControllerSettingsManager.MAX_CONTROL_OFFSET
    val editorState =
        TouchControlEditorState(
            isEditing = true,
            selectedControlKey = selectedControlKey,
            onSelect = { selectedControlKey = it },
            // Dragging is rendered locally by touchControlTransform. Commit only once when the
            // gesture ends so pointer motion never waits on the settings StateFlow.
            onDrag = { _, _, _ -> },
            onDragEnd = { key, dx, dy ->
                if (dx != 0f || dy != 0f) {
                    updateControl(key) {
                        it.copy(
                            offsetX = (it.offsetX + dx).coerceIn(-maxOffset, maxOffset),
                            offsetY = (it.offsetY + dy).coerceIn(-maxOffset, maxOffset),
                        )
                    }
                }
                persist()
            },
        )

    Box(modifier = Modifier.fillMaxSize()) {
        if (settings != null) {
            val overlay = allowTouchOverlay(controllerId)
            CompositionLocalProvider(
                LocalLemuroidPadTheme provides rememberTouchButtonAppearanceTheme(),
                LocalTouchControllerSettings provides settings,
                LocalTouchControlEditor provides editorState,
            ) {
                PadKit(
                    modifier = Modifier.fillMaxSize(),
                    onInputEvents = { events ->
                        if (events.any { it is InputEvent.Button && it.id == KeyEvent.KEYCODE_BUTTON_MODE && it.pressed }) {
                            onMenuPressed?.invoke()
                        }
                    },
                    hapticFeedbackType = HapticFeedbackType.NONE,
                ) {
                    val padKitScope = this
                    // Mirrors MobileGameScreen: no safe-drawing padding on the pad layer (the pads apply
                    // their own cutout margins), the game view only avoids the top cutout, and the same
                    // backdrop containers are drawn.
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize(),
                        constraintSet = GameScreenLayout.buildConstraintSet(isLandscape, overlay),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top)),
                        )
                        if (!isLandscape) {
                            Box(
                                modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER),
                            )
                        } else if (!overlay) {
                            PadContainer(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_CONTAINER))
                            PadContainer(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_CONTAINER))
                        }
                        padConfig.leftComposable(
                            padKitScope,
                            Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD),
                            settings,
                        )
                        padConfig.rightComposable(
                            padKitScope,
                            Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD),
                            settings,
                        )
                        Box(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER))
                    }
                }
            }
        }

        Card(
            modifier =
                Modifier.align(Alignment.TopCenter).widthIn(max = 760.dp)
                    .windowInsetsPadding(safeDrawingInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Row(
                modifier =
                    Modifier.horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { onClose?.invoke() ?: backDispatcher?.onBackPressed() },
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(come.asg.lemudroid.R.string.touch_editor_close),
                    )
                }
                Box {
                    TextButton(
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        onClick = { layoutMenuExpanded = true },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(controllerId.displayLabel(), maxLines = 1)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Open controller dropdown")
                        }
                    }
                    DropdownMenu(expanded = layoutMenuExpanded, onDismissRequest = { layoutMenuExpanded = false }) {
                        listOf(TouchControllerID.GB, TouchControllerID.GBC, TouchControllerID.GBA).forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.displayLabel()) },
                                onClick = {
                                    controllerId = item
                                    layoutMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                IconButton(modifier = Modifier.size(40.dp), onClick = { confirmReset = true }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = stringResource(come.asg.lemudroid.R.string.touch_editor_reset_layout),
                    )
                }
                IconButton(modifier = Modifier.size(40.dp), onClick = { persist() }) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = stringResource(come.asg.lemudroid.R.string.touch_editor_save_layout),
                    )
                }
            }
        }

        if (selectedControlKey == null) {
            Card(
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .windowInsetsPadding(safeDrawingInsets.only(WindowInsetsSides.Top))
                        .padding(top = 68.dp),
            ) {
                Text(
                    stringResource(come.asg.lemudroid.R.string.touch_editor_select_hint),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        BackHandler(enabled = selectedControlKey != null) { selectedControlKey = null }
        if (settings != null && selectedControlKey != null) {
            Box(
                modifier =
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().widthIn(max = 760.dp)
                        .windowInsetsPadding(
                            safeDrawingInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        )
                        .padding(top = 64.dp),
            ) {
                TouchControlEditorPanel(
                    selectedControlKey = selectedControlKey,
                    settings = settings,
                    onUpdateControl = { key, update -> updateControl(key, update) },
                    onPersist = { persist() },
                    onResetAll = {
                        coroutineScope.launch { settingsManager.resetSettings(controllerId, orientation) }
                        selectedControlKey = null
                    },
                    onDone = { selectedControlKey = null },
                )
            }
        }

        if (confirmReset) {
            AlertDialog(
                onDismissRequest = { confirmReset = false },
                title = {
                    Text(
                        stringResource(
                            come.asg.lemudroid.R.string.touch_editor_reset_orientation_title,
                            orientation.name.lowercase(),
                        ),
                    )
                },
                text = {
                    Text(
                        stringResource(
                            come.asg.lemudroid.R.string.touch_editor_reset_orientation_message,
                            orientation.name.lowercase(),
                        ),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch { settingsManager.resetSettings(controllerId, orientation) }
                            selectedControlKey = null
                            confirmReset = false
                        },
                    ) { Text(stringResource(come.asg.lemudroid.R.string.touch_editor_reset)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmReset = false
                    }) { Text(stringResource(come.asg.lemudroid.R.string.touch_editor_cancel)) }
                },
            )
        }
    }
}

@Composable
private fun TouchControllerID.displayLabel(): String =
    stringResource(
        when (this) {
            TouchControllerID.GB -> come.asg.lemudroid.R.string.touch_editor_game_boy
            TouchControllerID.GBC -> come.asg.lemudroid.R.string.touch_editor_game_boy_color
            TouchControllerID.NES -> come.asg.lemudroid.R.string.touch_editor_nes
            TouchControllerID.DESMUME -> come.asg.lemudroid.R.string.touch_editor_nintendo_ds
            TouchControllerID.MELONDS -> come.asg.lemudroid.R.string.touch_editor_nintendo_ds_melonds
            TouchControllerID.PSX -> come.asg.lemudroid.R.string.touch_editor_playstation
            TouchControllerID.PSX_DUALSHOCK -> come.asg.lemudroid.R.string.touch_editor_playstation_dualshock
            TouchControllerID.N64 -> come.asg.lemudroid.R.string.touch_editor_nintendo_64
            TouchControllerID.PSP -> come.asg.lemudroid.R.string.touch_editor_psp
            TouchControllerID.SNES -> come.asg.lemudroid.R.string.touch_editor_snes
            TouchControllerID.GBA -> come.asg.lemudroid.R.string.touch_editor_game_boy_advance
            TouchControllerID.GENESIS_3 -> come.asg.lemudroid.R.string.touch_editor_genesis_3
            TouchControllerID.GENESIS_6 -> come.asg.lemudroid.R.string.touch_editor_genesis_6
            TouchControllerID.ATARI2600 -> come.asg.lemudroid.R.string.touch_editor_atari_2600
            TouchControllerID.ATARI7800 -> come.asg.lemudroid.R.string.touch_editor_atari_7800
            TouchControllerID.SMS -> come.asg.lemudroid.R.string.touch_editor_master_system
            TouchControllerID.GG -> come.asg.lemudroid.R.string.touch_editor_game_gear
            TouchControllerID.ARCADE_4 -> come.asg.lemudroid.R.string.touch_editor_arcade_4
            TouchControllerID.ARCADE_6 -> come.asg.lemudroid.R.string.touch_editor_arcade_6
            TouchControllerID.LYNX -> come.asg.lemudroid.R.string.touch_editor_atari_lynx
            TouchControllerID.PCE -> come.asg.lemudroid.R.string.touch_editor_pc_engine
            TouchControllerID.NGP -> come.asg.lemudroid.R.string.touch_editor_neo_geo_pocket
            TouchControllerID.DOS -> come.asg.lemudroid.R.string.touch_editor_dos
            TouchControllerID.WS_LANDSCAPE -> come.asg.lemudroid.R.string.touch_editor_wonderswan_landscape
            TouchControllerID.WS_PORTRAIT -> come.asg.lemudroid.R.string.touch_editor_wonderswan_portrait
            TouchControllerID.NINTENDO_3DS -> come.asg.lemudroid.R.string.touch_editor_nintendo_3ds
        },
    )
