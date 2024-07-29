package com.swordfish.lemuroid.app.utils.android.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.app.utils.settings.rememberSafePreferenceBooleanSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberSafePreferenceIndexSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberSafePreferenceIntSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberSafePreferenceStringsSetSettingState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

@Composable
fun booleanPreferenceState(
    id: Int,
    default: Boolean,
) = booleanPreferenceState(stringResource(id = id), default)

@Composable
fun booleanPreferenceState(
    key: String,
    default: Boolean,
) = rememberSafePreferenceBooleanSettingState(
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
) = rememberSafePreferenceIndexSettingState(
    key = key,
    values = values,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)

@Composable
fun stringsSetPreferenceState(
    key: String,
    default: Set<String>,
) = rememberSafePreferenceStringsSetSettingState(
    key = key,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)

@Composable
fun intPreferenceState(
    key: String,
    default: Int,
) = rememberSafePreferenceIntSettingState(
    key = key,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current),
)
