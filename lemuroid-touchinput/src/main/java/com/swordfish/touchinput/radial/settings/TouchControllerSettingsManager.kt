package com.swordfish.touchinput.radial.settings

import android.content.SharedPreferences
import android.view.KeyEvent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max
import androidx.core.content.edit
import come.asg.lemudroid.common.compose.pxToDp
import gg.padkit.ids.Id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class TouchControllerSettingsManager(private val sharedPreferences: SharedPreferences) {
    enum class Orientation {
        PORTRAIT,
        LANDSCAPE,
    }

    @Serializable
    data class ControlSettings(
        val scale: Float = DEFAULT_CONTROL_SCALE,
        val opacity: Float = DEFAULT_CONTROL_OPACITY,
        val sensitivity: Float = DEFAULT_CONTROL_SENSITIVITY,
        val offsetX: Float = DEFAULT_CONTROL_OFFSET,
        val offsetY: Float = DEFAULT_CONTROL_OFFSET,
        val visible: Boolean = DEFAULT_CONTROL_VISIBLE,
    )

    @Serializable
    data class Settings(
        val scale: Float = DEFAULT_SCALE,
        val rotation: Float = DEFAULT_ROTATION,
        val marginX: Float = DEFAULT_MARGIN_X,
        val marginY: Float = DEFAULT_MARGIN_Y,
        val controls: Map<String, ControlSettings> = emptyMap(),
    )

    private fun computeInsetsPaddings(
        density: Density,
        insets: WindowInsets,
    ): PaddingValues {
        return PaddingValues(
            insets.getLeft(density, layoutDirection = LayoutDirection.Ltr).pxToDp(density),
            insets.getTop(density).pxToDp(density),
            insets.getRight(density, layoutDirection = LayoutDirection.Ltr).pxToDp(density),
            insets.getBottom(density).pxToDp(density),
        )
    }

    fun observeSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        density: Density,
        insets: WindowInsets,
    ): Flow<Settings> {
        val paddings = computeInsetsPaddings(density, insets)
        val horizontalPadding =
            max(
                paddings.calculateLeftPadding(LayoutDirection.Ltr),
                paddings.calculateRightPadding(LayoutDirection.Ltr),
            )
        val verticalPadding = paddings.calculateBottomPadding()
        val defaultSettings =
            Settings(
                scale = DEFAULT_SCALE,
                rotation = DEFAULT_ROTATION,
                marginX = horizontalPadding.value / MAX_MARGINS,
                marginY = verticalPadding.value / MAX_MARGINS,
            )
        val settingsKey = getPreferenceString(touchControllerID, orientation)
        lastDefaults[settingsKey] = defaultSettings
        val cachedStateFlow =
            cachedSettings.getOrPut(settingsKey) { MutableStateFlow(loadSettings(settingsKey)) }
        return cachedStateFlow.map { sanitize(it ?: defaultSettings) }
    }

    /**
     * Atomically updates one control against the latest in-memory state (no stale snapshots, no disk
     * write). Call [persist] when a gesture finishes.
     */
    fun updateControl(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        controlKey: String,
        transform: (ControlSettings) -> ControlSettings,
    ) {
        val settingsKey = getPreferenceString(touchControllerID, orientation)
        val flow = cachedSettings.getOrPut(settingsKey) { MutableStateFlow(loadSettings(settingsKey)) }
        val defaults = lastDefaults[settingsKey] ?: Settings()
        flow.update { stored ->
            val base = stored ?: defaults
            val current = base.controls[controlKey] ?: ControlSettings()
            base.copy(controls = base.controls + (controlKey to transform(current)))
        }
    }

    /** Writes the current in-memory settings to disk (asynchronous apply). */
    fun persist(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
    ) {
        val settingsKey = getPreferenceString(touchControllerID, orientation)
        val settings = cachedSettings[settingsKey]?.value ?: return
        sharedPreferences.edit {
            putString(settingsKey, JSON.encodeToString(Settings.serializer(), settings))
        }
    }

    suspend fun storeSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        settings: Settings,
    ) {
        Timber.d("Updating touch settings for $touchControllerID at $orientation to $settings")
        updateCachedSettings(touchControllerID, orientation, settings)
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                putString(
                    getPreferenceString(touchControllerID, orientation),
                    JSON.encodeToString(Settings.serializer(), settings),
                )
            }
        }
    }

    private fun updateCachedSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        settings: Settings?,
    ) {
        val cacheKey = getPreferenceString(touchControllerID, orientation)
        cachedSettings.getOrPut(cacheKey) { MutableStateFlow(settings) }.value = settings
    }

    suspend fun resetSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
    ) {
        updateCachedSettings(touchControllerID, orientation, null)
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                remove(getPreferenceString(touchControllerID, orientation))
            }
        }
    }

    private fun loadSettings(settingsKey: String): Settings? {
        val raw = sharedPreferences.getString(settingsKey, null) ?: return null
        return try {
            JSON.decodeFromString(Settings.serializer(), raw)
        } catch (e: Exception) {
            Timber.w(e, "Ignoring unreadable touch settings for $settingsKey")
            null
        }
    }

    /** Keeps persisted/edited values inside what the UI and renderer can handle. */
    private fun sanitize(settings: Settings): Settings {
        if (settings.controls.isEmpty()) return settings
        return settings.copy(
            controls =
                settings.controls.mapValues { (key, control) ->
                    control.copy(
                        scale = control.scale.coerceIn(MIN_CONTROL_SCALE, MAX_CONTROL_SCALE),
                        opacity = control.opacity.coerceIn(MIN_CONTROL_OPACITY, 1f),
                        sensitivity = control.sensitivity.coerceIn(MIN_SENSITIVITY, MAX_SENSITIVITY),
                        offsetX = control.offsetX.coerceIn(-MAX_CONTROL_OFFSET, MAX_CONTROL_OFFSET),
                        offsetY = control.offsetY.coerceIn(-MAX_CONTROL_OFFSET, MAX_CONTROL_OFFSET),
                        visible = control.visible || !isHideable(key),
                    )
                },
        )
    }

    companion object {
        data class ControlPreset(val key: String, val label: String)

        /** Single place that defines how a control id maps to its persisted settings key. */
        fun controlKey(id: Id): String = id.toString()

        val CONTROL_PRESETS =
            listOf(
                ControlPreset(controlKey(Id.DiscreteDirection(0)), "D-pad"),
                ControlPreset(controlKey(Id.ContinuousDirection(1)), "Left stick"),
                ControlPreset(controlKey(Id.ContinuousDirection(2)), "Right stick"),
                ControlPreset(controlKey(Id.DiscreteDirection(3)), "D-pad + left stick"),
                ControlPreset(controlKey(Id.DiscreteDirection(2)), "Right D-pad"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_A)), "A"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_B)), "B"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_X)), "X"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_Y)), "Y"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_L1)), "L1"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_L2)), "L2"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_R1)), "R1"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_R2)), "R2"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_START)), "Start"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT)), "Select"),
                ControlPreset(controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_MODE)), "Menu"),
                ControlPreset(controlKey(Id.Key(1001)), "Fast forward"),
            )

        /** The menu button is the only touch way into the in-game menu, so it can never be hidden. */
        val MENU_CONTROL_KEY = controlKey(Id.Key(KeyEvent.KEYCODE_BUTTON_MODE))

        fun isHideable(key: String): Boolean = key != MENU_CONTROL_KEY

        private val STICK_KEYS =
            setOf(controlKey(Id.ContinuousDirection(1)), controlKey(Id.ContinuousDirection(2)))

        fun isStick(key: String): Boolean = key in STICK_KEYS

        const val DEFAULT_SCALE = 0.5f
        const val DEFAULT_ROTATION = 0.0f
        const val DEFAULT_MARGIN_X = 0.0f
        const val DEFAULT_MARGIN_Y = 0.0f
        const val DEFAULT_CONTROL_SCALE = 1.0f
        const val DEFAULT_CONTROL_OPACITY = 1.0f
        const val DEFAULT_CONTROL_SENSITIVITY = 1.0f
        const val DEFAULT_CONTROL_OFFSET = 0.0f
        const val DEFAULT_CONTROL_VISIBLE = true

        const val MIN_CONTROL_SCALE = 0.5f
        const val MAX_CONTROL_SCALE = 2.0f
        const val MIN_CONTROL_OPACITY = 0.1f
        const val MIN_SENSITIVITY = 0.25f
        const val MAX_SENSITIVITY = 2.5f
        const val MAX_CONTROL_OFFSET = 2000f

        const val MAX_ROTATION = 45f
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 1.5f

        const val MAX_MARGINS = 96f

        // Shared by every manager instance so the editor and the game never see different caches.
        private val cachedSettings = ConcurrentHashMap<String, MutableStateFlow<Settings?>>()
        private val lastDefaults = ConcurrentHashMap<String, Settings>()
        private val JSON =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
    }

    private fun getPreferenceString(
        controllerID: TouchControllerID,
        orientation: Orientation,
    ): String {
        return "touch_controller_settings_${controllerID}_${orientation.ordinal}"
    }
}

val LocalTouchControllerSettings =
    staticCompositionLocalOf { TouchControllerSettingsManager.Settings() }
