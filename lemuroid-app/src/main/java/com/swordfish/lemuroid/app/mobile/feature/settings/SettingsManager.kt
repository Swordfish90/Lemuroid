package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates

class SettingsManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

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

    var simulateScreen: Boolean by SharedPreferencesDelegates.BooleanDelegate(
        sharedPreferences,
        getString(R.string.pref_key_shader),
        true
    )
}
