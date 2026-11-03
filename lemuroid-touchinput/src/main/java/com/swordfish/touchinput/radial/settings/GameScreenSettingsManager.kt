package com.swordfish.touchinput.radial.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Orientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Persists the user defined position and size of the emulated screen, independently per
 * [Orientation]. The values are normalized so they can be mapped directly onto the
 * GLRetroView viewport (a rectangle expressed in the [0, 1] range).
 *
 * This lets the user shrink and reposition the game image, for instance to fit it in the visible
 * area when a clip-on controller (such as the 8BitDo Flip Pad) physically covers part of the screen.
 */
class GameScreenSettingsManager(private val sharedPreferences: SharedPreferences) {
    @Serializable
    data class Settings(
        val scale: Float = DEFAULT_SCALE,
        val offsetX: Float = DEFAULT_OFFSET,
        val offsetY: Float = DEFAULT_OFFSET,
    )

    private val cachedSettings = mutableMapOf<String, MutableStateFlow<Settings?>>()

    fun observeSettings(orientation: Orientation): Flow<Settings> {
        val key = getPreferenceString(orientation)
        val cachedStateFlow =
            cachedSettings.getOrPut(key) {
                val currentSettings =
                    sharedPreferences.getString(key, null)
                        ?.let { Json.decodeFromString(Settings.serializer(), it) }
                MutableStateFlow(currentSettings)
            }
        return cachedStateFlow.map { it ?: Settings() }
    }

    suspend fun storeSettings(
        orientation: Orientation,
        settings: Settings,
    ) {
        Timber.d("Updating game screen settings at $orientation to $settings")
        updateCachedSettings(orientation, settings)
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                putString(
                    getPreferenceString(orientation),
                    Json.encodeToString(Settings.serializer(), settings),
                )
            }
        }
    }

    suspend fun resetSettings(orientation: Orientation) {
        updateCachedSettings(orientation, null)
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                remove(getPreferenceString(orientation))
            }
        }
    }

    private fun updateCachedSettings(
        orientation: Orientation,
        settings: Settings?,
    ) {
        val key = getPreferenceString(orientation)
        val cacheFlow = cachedSettings.getOrPut(key) { MutableStateFlow(settings) }
        cacheFlow.value = settings
    }

    private fun getPreferenceString(orientation: Orientation): String {
        return "game_screen_settings_${orientation.ordinal}"
    }

    companion object {
        const val DEFAULT_SCALE = 1.0f
        const val DEFAULT_OFFSET = 0.0f

        const val MIN_SCALE = 0.3f
        const val MAX_SCALE = 1.0f

        // Maximum translation as a fraction of the full surface, in each direction.
        const val MAX_OFFSET = 0.5f
    }
}
