package com.swordfish.lemuroid.app.utils.android.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.disk.rememberPreferenceBooleanSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberPreferenceIndexSettingState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

@Composable
fun booleanPreferenceState(
    id: Int,
    default: Boolean,
) = rememberPreferenceBooleanSettingState(
    key = stringResource(id = id),
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)

@Composable
fun booleanPreferenceState(
    key: String,
    default: Boolean,
) = rememberPreferenceBooleanSettingState(
    key = key,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)

@Composable
fun indexPreferenceState(
    id: Int,
    default: String,
    values: List<String>,
) = indexPreferenceState(stringResource(id), default, values)

@Composable
fun indexPreferenceState(
    key: String,
    default: String,
    values: List<String>,
) = rememberPreferenceIndexSettingState(
    key = key,
    values = values,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)
