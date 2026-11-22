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
fun rememberSafePreferenceStringsSetSettingState(
    key: String,
    defaultValue: Set<String>,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): SafeStringsSetPreferenceSettingValueState {
    return remember(key, preferences) {
        SafeStringsSetPreferenceSettingValueState(preferences, key, defaultValue)
    }
}

class SafeStringsSetPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    private val defaultValue: Set<String>,
) : SettingValueState<Set<String>> {
    private var _value by mutableStateOf(readPreference())

    private fun readPreference(): Set<String> {
        if (!preferences.contains(key)) {
            return defaultValue
        }
        val strVal = runCatching { preferences.getString(key, null) }.getOrNull()
        if (strVal != null) {
            return if (strVal.isEmpty() || strVal == "__NONE__") {
                emptySet()
            } else {
                strVal.split(",").filter { it.isNotBlank() }.toSet()
            }
        }
        val setVal = runCatching { preferences.getStringSet(key, null) }.getOrNull()
        if (setVal != null) {
            return setVal
        }
        return defaultValue
    }

    override var value: Set<String>
        set(newSet) {
            _value = newSet
            val serialized = if (newSet.isEmpty()) "__NONE__" else newSet.joinToString(",")
            preferences.edit {
                putString(key, serialized)
            }
        }
        get() = _value.toSet()

    override fun reset() {
        value = defaultValue
    }
}
