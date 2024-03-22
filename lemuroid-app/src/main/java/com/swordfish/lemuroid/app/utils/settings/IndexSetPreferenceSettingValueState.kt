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
fun rememberPreferenceSetSettingState(
    key: String,
    values: List<String>,
    defaultValue: Set<String>,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): IndexSetPreferenceSettingValueState {
    return remember {
        IndexSetPreferenceSettingValueState(preferences, key, values, defaultValue)
    }
}

class IndexSetPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    private val values: List<String>,
    private val defaultValue: Set<String>,
) : SettingValueState<Set<Int>> {
    private var _value by mutableStateOf(preferences.getStringSet(key, defaultValue)!!)

    override var value: Set<Int>
        set(index) {
            _value = index.map { values[it] }.toSet()
            preferences.edit { putStringSet(key, _value) }
        }
        get() =
            _value
                .map { values.indexOf(it) }
                .toSet()

    override fun reset() {
        value =
            defaultValue
                .map { values.indexOf(it) }
                .toSet()
    }
}
