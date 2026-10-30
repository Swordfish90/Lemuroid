package com.swordfish.touchinput.radial.settings

import android.content.SharedPreferences
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TouchControllerSettingsManager(
    private val sharedPreferences: SharedPreferences,
    private val systemId: String? = null,
) {
    enum class Orientation {
        PORTRAIT,
        LANDSCAPE,
    }

    @Serializable
    data class Appearance(
        val scale: Float = DEFAULT_SCALE,
        val opacity: Float = 1f,
        val transparentBackground: Boolean = false,
    )

    @Serializable
    data class Settings(
        val scale: Float = DEFAULT_SCALE,
        val rotation: Float = DEFAULT_ROTATION,
        val marginX: Float = DEFAULT_MARGIN_X,
        val marginY: Float = DEFAULT_MARGIN_Y,
        val opacity: Float = 1f,
        val transparentBackground: Boolean = false,
        val useSharedAppearance: Boolean = false,
    )

    private val writes = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    fun observeSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        density: Density,
        insets: WindowInsets,
    ): Flow<Settings> {
        val horizontalInset =
            maxOf(
                insets.getLeft(density, LayoutDirection.Ltr),
                insets.getRight(density, LayoutDirection.Ltr),
            ) / density.density
        val defaults =
            Settings(
                marginX = horizontalInset / MAX_MARGINS,
                marginY = insets.getBottom(density) / density.density / MAX_MARGINS,
                useSharedAppearance = true,
            )
        val key = getPreferenceString(touchControllerID, orientation)
        val legacyKey = legacyPreferenceString(touchControllerID, orientation)
        return changes().map {
            val stored = sharedPreferences.getString(if (sharedPreferences.contains(key)) key else legacyKey, null)
            val settings = decodeSettings(stored) ?: defaults
            if (settings.useSharedAppearance) {
                val appearance = readAppearance()
                settings.copy(
                    scale = appearance.scale,
                    opacity = appearance.opacity,
                    transparentBackground = appearance.transparentBackground,
                )
            } else {
                settings
            }
        }.distinctUntilChanged()
    }

    fun observeAppearance(): Flow<Appearance> = changes().map { readAppearance() }.distinctUntilChanged()

    suspend fun storeAppearance(appearance: Appearance) {
        write { putString(APPEARANCE_KEY, json.encodeToString(Appearance.serializer(), appearance)) }
    }

    suspend fun storeSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        settings: Settings,
    ) {
        write {
            putString(
                getPreferenceString(touchControllerID, orientation),
                json.encodeToString(Settings.serializer(), settings),
            )
        }
    }

    suspend fun resetSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
    ) {
        // Keep a reset marker so a legacy layout is not restored on the next launch.
        write { putString(getPreferenceString(touchControllerID, orientation), "null") }
    }

    private fun decodeSettings(value: String?): Settings? =
        value?.takeUnless { it == "null" }?.let {
            runCatching { json.decodeFromString(Settings.serializer(), it) }.getOrNull()
        }

    private fun readAppearance(): Appearance =
        sharedPreferences.getString(APPEARANCE_KEY, null)?.let {
            runCatching { json.decodeFromString(Appearance.serializer(), it) }.getOrNull()
        } ?: Appearance()

    private fun changes(): Flow<Unit> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(Unit) }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            trySend(Unit)
            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.conflate()

    private suspend fun write(update: SharedPreferences.Editor.() -> Unit) {
        writes.withLock {
            withContext(Dispatchers.IO) { sharedPreferences.edit(action = update) }
        }
    }

    private fun getPreferenceString(
        controllerID: TouchControllerID,
        orientation: Orientation,
    ): String =
        if (systemId == null) {
            legacyPreferenceString(controllerID, orientation)
        } else {
            "touch_controller_settings_${systemId}_${controllerID}_${orientation.ordinal}"
        }

    private fun legacyPreferenceString(
        controllerID: TouchControllerID,
        orientation: Orientation,
    ) = "touch_controller_settings_${controllerID}_${orientation.ordinal}"

    companion object {
        private const val APPEARANCE_KEY = "touch_controller_shared_appearance"
        const val DEFAULT_SCALE = 0.5f
        const val DEFAULT_ROTATION = 0.0f
        const val DEFAULT_MARGIN_X = 0.0f
        const val DEFAULT_MARGIN_Y = 0.0f
        const val MAX_ROTATION = 45f
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 1.5f
        const val MAX_MARGINS = 96f
    }
}
