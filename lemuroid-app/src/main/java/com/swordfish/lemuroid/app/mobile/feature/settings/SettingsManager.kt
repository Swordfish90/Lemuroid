package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class SettingsManager(private val context: Context, sharedPreferences: Lazy<SharedPreferences>) {

    // TODO COROUTINE... Make it async
    private val flowSharedProcess = FlowSharedPreferences(sharedPreferences.get())

    private fun getString(resId: Int) = context.getString(resId)

    suspend fun autoSave() = booleanPreference(R.string.pref_key_autosave, true)

    suspend fun hapticFeedbackMode() = stringPreference(R.string.pref_key_haptic_feedback_mode, "press")

    suspend fun lowLatencyAudio() = booleanPreference(R.string.pref_key_low_latency_audio, false)

    suspend fun screenFilter() = stringPreference(
        R.string.pref_key_shader_filter,
        context.resources.getStringArray(R.array.pref_key_shader_filter_values).first()
    )

    suspend fun tiltSensitivity() = floatPreference(R.string.pref_key_tilt_sensitivity_index, 10, 0.6f)

    suspend fun autoSaveSync() = booleanPreference(R.string.pref_key_save_sync_auto, false)

    suspend fun syncSaves() = booleanPreference(R.string.pref_key_save_sync_enable, true)

    suspend fun syncStatesCores() = stringSetPreference(R.string.pref_key_save_sync_cores, setOf())

    suspend fun enableRumble() = booleanPreference(R.string.pref_key_enable_rumble, false)

    suspend fun enableDeviceRumble() = booleanPreference(R.string.pref_key_enable_device_rumble, false)

    suspend fun cacheSizeBytes() = stringPreference(
        R.string.pref_key_max_cache_size,
        CacheCleaner.getDefaultCacheLimit().toString()
    )

    suspend fun allowDirectGameLoad() = booleanPreference(R.string.pref_key_allow_direct_game_load, true)

    private suspend fun booleanPreference(keyId: Int, default: Boolean): Boolean {
        return flowSharedProcess.getBoolean(getString(keyId), default)
            .asFlow()
            .first()
    }

    private suspend fun stringPreference(keyId: Int, default: String): String {
        return flowSharedProcess.getString(getString(keyId), default)
            .asFlow()
            .first()
    }

    private suspend fun stringSetPreference(keyId: Int, default: Set<String>): Set<String> {
        return flowSharedProcess.getStringSet(getString(keyId), default)
            .asFlow()
            .first()
    }

    private suspend fun floatPreference(keyId: Int, ticks: Int, default: Float): Float {
        return flowSharedProcess.getInt(getString(keyId), floatToIndex(default, ticks))
            .asFlow()
            .map { indexToFloat(it, ticks) }
            .first()
    }

    private fun indexToFloat(index: Int, ticks: Int): Float = index.toFloat() / ticks.toFloat()

    private fun floatToIndex(value: Float, ticks: Int): Int = (value * ticks).roundToInt()
}
