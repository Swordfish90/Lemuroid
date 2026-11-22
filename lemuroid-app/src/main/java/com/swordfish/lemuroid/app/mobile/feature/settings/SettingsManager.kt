package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.HDModeQuality
import com.swordfish.lemuroid.common.math.Fraction
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsManager(private val context: Context, sharedPreferences: Lazy<SharedPreferences>) {
    private val sharedPreferences by lazy { FlowSharedPreferences(sharedPreferences.get()) }

    private fun getString(resId: Int) = context.getString(resId)

    suspend fun autoSave() = booleanPreference(R.string.pref_key_autosave, true)

    suspend fun hapticFeedbackMode() = stringPreference(R.string.pref_key_haptic_feedback_mode, "press")

    suspend fun lowLatencyAudio() = booleanPreference(R.string.pref_key_low_latency_audio, false)

    suspend fun screenFilter() =
        stringPreference(
            R.string.pref_key_shader_filter,
            context.resources.getStringArray(R.array.pref_key_shader_filter_values).first(),
        )

    suspend fun hdMode() = booleanPreference(R.string.pref_key_hd_mode, false)

    suspend fun viewportAlignment() = stringPreference(R.string.pref_key_viewport_alignment, "center")

    suspend fun fastForwardSpeed(): Int =
        withContext(Dispatchers.IO) {
            val key = getString(R.string.pref_key_fast_forward_speed)
            val asInt = runCatching { sharedPreferences.getInt(key, 2).get() }.getOrNull()
            if (asInt != null) return@withContext asInt.coerceIn(2, 5)
            val asStr = runCatching { sharedPreferences.getString(key, "2").get() }.getOrNull()
            (asStr?.toIntOrNull() ?: 2).coerceIn(2, 5)
        }

    suspend fun hdModeQuality() = HDModeQuality.parse(intPreference(R.string.pref_key_hd_mode_quality, 1))

    suspend fun tiltSensitivity() = floatPreference(R.string.pref_key_tilt_sensitivity_index, 10, 6)

    suspend fun autoSaveSync() = booleanPreference(R.string.pref_key_save_sync_auto, false)

    suspend fun syncSaves() = booleanPreference(R.string.pref_key_save_sync_enable, true)

    suspend fun syncStatesCores() = stringSetPreference(R.string.pref_key_save_sync_cores, setOf())

    suspend fun enableRumble() = booleanPreference(R.string.pref_key_enable_rumble, false)

    suspend fun enableDeviceRumble() = booleanPreference(R.string.pref_key_enable_device_rumble, false)

    suspend fun enableImmersiveMode() = booleanPreference(R.string.pref_key_enable_immersive_mode, false)

    suspend fun cacheSizeBytes() =
        stringPreference(
            R.string.pref_key_max_cache_size,
            CacheCleaner.getDefaultCacheLimit().toString(),
        )

    suspend fun allowDirectGameLoad() = booleanPreference(R.string.pref_key_allow_direct_game_load, true)

    suspend fun directPlayOnTap() = booleanPreference(R.string.pref_key_direct_play_on_tap, false)

    fun directPlayOnTapFlow() = sharedPreferences.getBoolean(getString(R.string.pref_key_direct_play_on_tap), false).asFlow()

    suspend fun screenScraperUser() = stringPreference(R.string.pref_key_screenscraper_user, "")

    suspend fun screenScraperPassword() = stringPreference(R.string.pref_key_screenscraper_password, "")

    suspend fun screenScraperDevId() = stringPreference(R.string.pref_key_screenscraper_devid, "")

    suspend fun screenScraperDevPassword() = stringPreference(R.string.pref_key_screenscraper_devpassword, "")

    suspend fun screenScraperBoxArtFormat() = stringPreference(R.string.pref_key_screenscraper_box_art_format, "2D")

    suspend fun scraperPriority() = stringPreference(R.string.pref_key_scraper_priority, "MIXED")

    suspend fun screenScraperMediaSources(): Set<String> =
        withContext(Dispatchers.IO) {
            val key = getString(R.string.pref_key_screenscraper_media_sources)
            val strVal = runCatching { sharedPreferences.getString(key, "__UNSET__").get() }.getOrNull()
            if (strVal != null && strVal != "__UNSET__") {
                return@withContext if (strVal.isEmpty() || strVal == "__NONE__") {
                    emptySet()
                } else {
                    strVal.split(",").filter { it.isNotBlank() }.toSet()
                }
            }
            val setVal = runCatching { sharedPreferences.getStringSet(key, emptySet()).get() }.getOrNull()
            setVal ?: emptySet()
        }

    fun gameSortOrderFlow(): kotlinx.coroutines.flow.Flow<String> =
        sharedPreferences.getString(getString(R.string.pref_key_game_sort_order), "NAME_ASC").asFlow()

    fun setGameSortOrder(sortOrder: String) {
        sharedPreferences.getString(getString(R.string.pref_key_game_sort_order), "NAME_ASC").set(sortOrder)
    }

    fun gameViewModeFlow(): kotlinx.coroutines.flow.Flow<String> =
        sharedPreferences.getString(getString(R.string.pref_key_game_view_mode), "LIST").asFlow()

    fun setGameViewMode(viewMode: String) {
        sharedPreferences.getString(getString(R.string.pref_key_game_view_mode), "LIST").set(viewMode)
    }

    private suspend fun booleanPreference(
        keyId: Int,
        default: Boolean,
    ): Boolean =
        withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(getString(keyId), default)
                .asFlow()
                .first()
        }

    private suspend fun stringPreference(
        keyId: Int,
        default: String,
    ): String =
        withContext(Dispatchers.IO) {
            sharedPreferences.getString(getString(keyId), default)
                .asFlow()
                .first()
        }

    private suspend fun stringSetPreference(
        keyId: Int,
        default: Set<String>,
    ): Set<String> =
        withContext(Dispatchers.IO) {
            sharedPreferences.getStringSet(getString(keyId), default)
                .asFlow()
                .first()
        }

    private suspend fun floatPreference(
        keyId: Int,
        denominator: Int,
        defaultNumerator: Int,
    ): Float =
        withContext(Dispatchers.IO) {
            sharedPreferences.getInt(getString(keyId), defaultNumerator)
                .asFlow()
                .map { Fraction(it, denominator).floatValue }
                .first()
        }

    private suspend fun intPreference(
        keyId: Int,
        default: Int,
    ): Int =
        withContext(Dispatchers.IO) {
            sharedPreferences.getInt(getString(keyId), default)
                .asFlow()
                .first()
        }
}
