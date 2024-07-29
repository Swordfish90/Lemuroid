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
fun rememberSafePreferenceIntSettingState(
    key: String,
    defaultValue: Int,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): SafeIntPreferenceSettingValueState {
    return remember {
        SafeIntPreferenceSettingValueState(
            preferences = preferences,
            key = key,
            defaultValue = defaultValue,
        )
    }
}

class SafeIntPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    val defaultValue: Int = 0,
) : SettingValueState<Int> {
    private var _value by mutableStateOf(preferences.safeGetInt(key, defaultValue))

    override var value: Int
        set(value) {
            _value = value
            preferences.edit { putInt(key, value) }
        }
        get() = _value

    override fun reset() {
        value = defaultValue
    }
}
