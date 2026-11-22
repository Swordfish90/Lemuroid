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
fun rememberSafePreferenceIndexSettingState(
    key: String,
    values: List<String>,
    defaultValue: String,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): SafeIndexPreferenceSettingValueState {
    return remember {
        SafeIndexPreferenceSettingValueState(preferences, key, values, defaultValue)
    }
}

class SafeIndexPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    private val values: List<String>,
    private val defaultValue: String,
) : SettingValueState<Int> {
    private var _value by mutableStateOf(preferences.safeGetString(key, defaultValue))

    override var value: Int
        set(index) {
            _value = values[index]
            preferences.edit { putString(key, _value) }
        }
        get() = values.indexOf(_value)

    override fun reset() {
        value = values.indexOf(defaultValue)
    }
}

@Composable
fun rememberSafePreferenceIntIndexSettingState(
    key: String,
    values: List<Int>,
    defaultValue: Int,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): SafeIntIndexPreferenceSettingValueState {
    return remember {
        SafeIntIndexPreferenceSettingValueState(preferences, key, values, defaultValue)
    }
}

class SafeIntIndexPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    private val values: List<Int>,
    private val defaultValue: Int,
) : SettingValueState<Int> {
    private var _value by mutableStateOf(getStoredValue())

    private fun getStoredValue(): Int {
        val asInt = runCatching { preferences.getInt(key, defaultValue) }.getOrNull()
        if (asInt != null && asInt in values) return asInt
        val asStr = runCatching { preferences.getString(key, null) }.getOrNull()
        val parsed = asStr?.toIntOrNull()
        if (parsed != null && parsed in values) return parsed
        return defaultValue
    }

    override var value: Int
        set(index) {
            val speed = values.getOrElse(index) { defaultValue }
            _value = speed
            preferences.edit { putInt(key, speed) }
        }
        get() {
            val idx = values.indexOf(_value)
            return if (idx >= 0) idx else values.indexOf(defaultValue)
        }

    override fun reset() {
        value = values.indexOf(defaultValue)
    }
}
