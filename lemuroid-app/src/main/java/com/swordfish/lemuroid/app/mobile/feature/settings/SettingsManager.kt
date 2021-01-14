package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

class SettingsManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        SharedPreferencesHelper.getSharedPreferences(context)

    private fun getString(resId: Int) = context.getString(resId)

    var autoSave: Boolean by SharedPreferencesDelegates.BooleanDelegate(
        sharedPreferences,
        getString(R.string.pref_key_autosave),
        true
    )

    var vibrateOnTouch: Boolean by SharedPreferencesDelegates.BooleanDelegate(
        sharedPreferences,
        getString(R.string.pref_key_vibrate_on_touch),
        true
    )

    var screenFilter: String by SharedPreferencesDelegates.StringDelegate(
        sharedPreferences,
        getString(R.string.pref_key_shader_filter),
        context.resources.getStringArray(R.array.pref_key_shader_filter_values).first()
    )

    var tiltSensitivity: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getString(R.string.pref_key_tilt_sensitivity_index),
        defaultIndex = 6
    )

    var gamepadsEnabled: Boolean by SharedPreferencesDelegates.BooleanDelegate(
        sharedPreferences,
        getString(R.string.pref_key_enable_gamepads),
        true
    )

    var autoSaveSync: Boolean by SharedPreferencesDelegates.BooleanDelegate(
        sharedPreferences,
        getString(R.string.pref_key_save_sync_auto),
        false
    )

    var syncSaves: Boolean by SharedPreferencesDelegates.BooleanDelegate(
        sharedPreferences,
        getString(R.string.pref_key_save_sync_enable),
        true
    )

    var syncStatesCores: Set<String> by SharedPreferencesDelegates.StringSetDelegate(
        sharedPreferences,
        getString(R.string.pref_key_save_sync_cores),
        setOf()
    )
}
