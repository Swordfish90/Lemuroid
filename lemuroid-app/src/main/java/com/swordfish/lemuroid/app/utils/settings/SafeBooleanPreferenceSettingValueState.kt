package com.swordfish.lemuroid.app.utils.settings

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.alorma.compose.settings.storage.base.SettingValueState

@Composable
fun rememberSafePreferenceBooleanSettingState(
    key: String,
    defaultValue: Boolean,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): SafeBooleanPreferenceSettingValueState {
    return remember {
        SafeBooleanPreferenceSettingValueState(
            preferences = preferences,
            key = key,
            defaultValue = defaultValue,
        )
    }
}

class SafeBooleanPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    val defaultValue: Boolean = false,
) : SettingValueState<Boolean> {
    private var _value by mutableStateOf(preferences.safeGetBoolean(key, defaultValue))

    override var value: Boolean
        set(value) {
            _value = value
            preferences.edit { putBoolean(key, value) }
        }
        get() = _value

    override fun reset() {
        value = defaultValue
    }
}
