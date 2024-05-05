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
fun rememberPreferenceStringsSetSettingState(
    key: String,
    defaultValue: Set<String>,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): StringsSetPreferenceSettingValueState {
    return remember {
        StringsSetPreferenceSettingValueState(preferences, key, defaultValue)
    }
}

class StringsSetPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    private val defaultValue: Set<String>,
) : SettingValueState<Set<String>> {
    private var _value by mutableStateOf(preferences.getStringSet(key, defaultValue)!!)

    override var value: Set<String>
        set(index) {
            _value = index
            preferences.edit { putStringSet(key, _value) }
        }
        get() = _value.toSet()

    override fun reset() {
        value = defaultValue
    }
}
