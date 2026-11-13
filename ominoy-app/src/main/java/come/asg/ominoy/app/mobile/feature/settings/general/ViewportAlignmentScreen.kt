package come.asg.ominoy.app.mobile.feature.settings.general

import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.swordfish.touchinput.radial.LocalOminoyPadTheme
import com.swordfish.touchinput.radial.settings.LocalTouchControllerSettings
import com.swordfish.touchinput.radial.settings.TouchControllerID
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.game.GameScreenLayout
import come.asg.ominoy.app.mobile.feature.game.PadContainer
import come.asg.ominoy.app.utils.android.settings.rememberTouchButtonAppearanceTheme
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType

private enum class ViewportPosition(
    val label: String,
    val preferenceValue: String,
) {
    TOP("Top", "top"),
    CENTER("Center", "center"),
    BOTTOM("Bottom", "bottom"),
    CUSTOM("Custom", "custom"),
}

private const val CUSTOM_POSITION_KEY = "viewport_custom_position"

@Composable
fun ViewportAlignmentScreen(
    modifier: Modifier = Modifier,
    preferences: SharedPreferences,
    settingsManager: TouchControllerSettingsManager,
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current
    val density = LocalDensity.current
    var controllerId by rememberSaveable { mutableStateOf(TouchControllerID.GBA.name) }
    val initialPosition = preferences.getString("viewport_alignment", "center") ?: "center"
    var selectedPosition by rememberSaveable {
        mutableStateOf(
            ViewportPosition.values().firstOrNull { it.preferenceValue == initialPosition }?.name
                ?: ViewportPosition.CENTER.name,
        )
    }
    var customPosition by rememberSaveable {
        mutableStateOf(preferences.getInt(CUSTOM_POSITION_KEY, 500).coerceIn(0, 1000) / 1000f)
    }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var positionMenuExpanded by remember { mutableStateOf(false) }

    val orientation = TouchControllerSettingsManager.Orientation.PORTRAIT
    val settings =
        settingsManager
            .observeSettings(
                TouchControllerID.valueOf(controllerId),
                orientation,
                androidx.compose.ui.platform.LocalDensity.current,
                WindowInsets.displayCutout,
            ).collectAsState(null)
            .value
    val viewportPosition = ViewportPosition.valueOf(selectedPosition)
    val selectedControllerId = TouchControllerID.valueOf(controllerId)
    val padConfig = remember(selectedControllerId) { TouchControllerID.getConfig(selectedControllerId) }

    fun selectPosition(position: ViewportPosition) {
        selectedPosition = position.name
        positionMenuExpanded = false
    }

    fun saveAndClose() {
        preferences
            .edit()
            .putString("viewport_alignment", viewportPosition.preferenceValue)
            .putInt(CUSTOM_POSITION_KEY, (customPosition * 1000f).toInt())
            .apply()
        Toast
            .makeText(
                context,
                context.getString(R.string.touch_editor_changes_saved),
                Toast.LENGTH_SHORT,
            ).show()
        backDispatcher?.onBackPressed()
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (settings != null) {
            CompositionLocalProvider(
                LocalOminoyPadTheme provides rememberTouchButtonAppearanceTheme(),
                LocalTouchControllerSettings provides settings,
            ) {
                PadKit(
                    modifier = Modifier.fillMaxSize(),
                    onInputEvents = {},
                    hapticFeedbackType = HapticFeedbackType.NONE,
                ) {
                    val padKitScope = this
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize(),
                        constraintSet =
                            GameScreenLayout.buildConstraintSet(
                                isLandscape = false,
                                allowTouchOverlay = true,
                            ),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top)),
                        ) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val screenAspectRatio =
                                    if (selectedControllerId == TouchControllerID.GBA) {
                                        240f / 160f
                                    } else {
                                        160f / 144f
                                    }
                                val screenHeight = (maxWidth / screenAspectRatio).coerceAtMost(maxHeight)
                                val availableHeight = (maxHeight - screenHeight).coerceAtLeast(0.dp)
                                val screenOffset =
                                    when (viewportPosition) {
                                        ViewportPosition.TOP -> 0.dp
                                        ViewportPosition.CENTER -> availableHeight / 2f
                                        ViewportPosition.BOTTOM -> availableHeight
                                        ViewportPosition.CUSTOM -> availableHeight * customPosition
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .width(maxWidth)
                                            .height(screenHeight)
                                            .offset(y = screenOffset)
                                            .then(
                                                if (viewportPosition == ViewportPosition.CUSTOM) {
                                                    Modifier.pointerInput(availableHeight) {
                                                        detectDragGestures { change, dragAmount ->
                                                            change.consume()
                                                            val availablePx =
                                                                with(density) {
                                                                    availableHeight.toPx()
                                                                }
                                                            if (availablePx > 0f) {
                                                                customPosition =
                                                                    (customPosition + dragAmount.y / availablePx)
                                                                        .coerceIn(
                                                                            0f,
                                                                            1f,
                                                                        )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Modifier
                                                },
                                            ).background(Color.White),
                                )
                            }
                        }
                        PadContainer(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER))
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
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    ).padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { backDispatcher?.onBackPressed() },
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close viewport alignment editor")
                }
                Box {
                    TextButton(
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        onClick = { typeMenuExpanded = true },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedControllerId.name, maxLines = 1)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Open controller dropdown")
                        }
                    }
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false },
                    ) {
                        listOf(TouchControllerID.GB, TouchControllerID.GBC, TouchControllerID.GBA).forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    controllerId = item.name
                                    typeMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Box {
                    TextButton(
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        onClick = { positionMenuExpanded = true },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(viewportPosition.label, maxLines = 1)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Open position dropdown")
                        }
                    }
                    DropdownMenu(
                        expanded = positionMenuExpanded,
                        onDismissRequest = { positionMenuExpanded = false },
                    ) {
                        ViewportPosition.values().forEach { position ->
                            DropdownMenuItem(
                                text = { Text(position.label) },
                                onClick = { selectPosition(position) },
                            )
                        }
                    }
                }
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = ::saveAndClose,
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save viewport alignment")
                }
            }
        }
    }
}
