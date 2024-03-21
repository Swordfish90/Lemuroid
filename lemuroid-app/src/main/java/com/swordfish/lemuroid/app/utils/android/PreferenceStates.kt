package com.swordfish.lemuroid.app.utils.android

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.swordfish.lemuroid.app.utils.settings.rememberPreferenceSetSettingState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

@Composable
fun indexSetPreferenceState(
    key: String,
    default: Set<String>,
    values: List<String>
) = rememberPreferenceSetSettingState(
    key = key,
    values = values,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current)
)
