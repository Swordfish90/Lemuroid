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
import com.swordfish.lemuroid.common.math.Fraction

@Composable
fun rememberFractionPreferenceSettingState(
    key: String,
    denominator: Int,
    defaultNumerator: Int,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): FractionPreferenceSettingValueState {
    return remember {
        FractionPreferenceSettingValueState(preferences, key, denominator, defaultNumerator)
    }
}

class FractionPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    private val key: String,
    private val denominator: Int,
    private val defaultNumerator: Int,
) : SettingValueState<Float> {

    private var _value by mutableStateOf(
        Fraction(preferences.getInt(key, defaultNumerator), denominator)
    )

    override var value: Float
        set(newNumerator) {
            _value = Fraction.fromValue(newNumerator, denominator)
            preferences.edit(true) { putInt(key, _value.numerator) }
        }
        get() {
            return _value.floatValue
        }

    override fun reset() {
        value = Fraction(defaultNumerator, denominator).floatValue
    }
}
