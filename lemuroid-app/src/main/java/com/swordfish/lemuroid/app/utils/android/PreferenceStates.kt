package com.swordfish.lemuroid.app.utils.android

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.swordfish.lemuroid.app.utils.settings.rememberPreferenceStringsSetSettingState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

@Composable
fun stringsSetPreferenceState(
    key: String,
    default: Set<String>,
) = rememberPreferenceStringsSetSettingState(
    key = key,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)
