package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class RxSettingsManager(private val context: Context, sharedPreferences: Lazy<SharedPreferences>) {

    private val rxSharedPreferences = Single.fromCallable {
        RxSharedPreferences.create(sharedPreferences.get())
    }

    private fun getString(resId: Int) = context.getString(resId)

    val autoSave = booleanPreference(R.string.pref_key_autosave, true)

    val hapticFeedbackMode = stringPreference(R.string.pref_key_haptic_feedback_mode, "press")

    val lowLatencyAudio = booleanPreference(R.string.pref_key_low_latency_audio, false)

    val screenFilter = stringPreference(
        R.string.pref_key_shader_filter,
        context.resources.getStringArray(R.array.pref_key_shader_filter_values).first()
    )

    val tiltSensitivity = floatPreference(R.string.pref_key_tilt_sensitivity_index, 10, 0.6f)

    val autoSaveSync = booleanPreference(R.string.pref_key_save_sync_auto, false)

    val syncSaves = booleanPreference(R.string.pref_key_save_sync_enable, true)

    val syncStatesCores = stringSetPreference(R.string.pref_key_save_sync_cores, setOf())

    val enableRumble = booleanPreference(R.string.pref_key_enable_rumble, true)

    val enableDeviceRumble = booleanPreference(R.string.pref_key_enable_device_rumble, true)

    val cacheSizeBytes = stringPreference(
        R.string.pref_key_max_cache_size,
        Single.fromCallable { CacheCleaner.getDefaultCacheLimit().toString() }
    )

    val allowDirectGameLoad = booleanPreference(R.string.pref_key_allow_direct_game_load, true)

    private fun booleanPreference(keyId: Int, default: Boolean): Single<Boolean> {
        return rxSharedPreferences.flatMap {
            it.getBoolean(getString(keyId), default)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(default)
        }
    }

    private fun stringPreference(keyId: Int, default: String): Single<String> {
        return stringPreference(keyId, Single.just(default))
    }

    private fun stringPreference(keyId: Int, default: Single<String>): Single<String> {
        return Singles.zip(rxSharedPreferences, default).flatMap { (preferences, defaultValue) ->
            preferences.getString(getString(keyId), defaultValue)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(defaultValue)
        }
    }

    private fun stringSetPreference(keyId: Int, default: Set<String>): Single<Set<String>> {
        return rxSharedPreferences.flatMap {
            it.getStringSet(getString(keyId), default)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(default)
        }
    }

    private fun floatPreference(keyId: Int, ticks: Int, default: Float): Single<Float> {
        return rxSharedPreferences.flatMap { sharedPreferences ->
            sharedPreferences.getInteger(getString(keyId), floatToIndex(default, ticks))
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(floatToIndex(default, ticks))
                .map { indexToFloat(it, ticks) }
        }
    }

    private fun indexToFloat(index: Int, ticks: Int): Float = index.toFloat() / ticks.toFloat()

    private fun floatToIndex(value: Float, ticks: Int): Int = (value * ticks).roundToInt()
}
