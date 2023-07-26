package com.swordfish.lemuroid.app.utils.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberFractionPreferenceSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberPreferenceSetSettingState
import com.swordfish.lemuroid.app.utils.settings.rememberPreferenceIndexSettingState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

@Composable
fun SettingsSmallGroup(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            if (title != null) {
                SettingsGroupTitleSmall(title)
            }
            content()
        }
    }
}

@Composable
private fun SettingsGroupTitleSmall(title: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val primary = MaterialTheme.colorScheme.primary
        val titleStyle = MaterialTheme.typography.labelLarge.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}

@Composable
fun booleanPreferenceState(
    id: Int,
    default: Boolean
) = rememberPreferenceBooleanSettingState(
    key = stringResource(id = id),
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current)
)

@Composable
fun booleanPreferenceState(
    key: String,
    default: Boolean
) = rememberPreferenceBooleanSettingState(
    key = key,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current)
)

@Composable
fun indexPreferenceState(
    id: Int,
    default: String,
    values: List<String>
) = indexPreferenceState(stringResource(id), default, values)

@Composable
fun indexPreferenceState(
    key: String,
    default: String,
    values: List<String>
) = rememberPreferenceIndexSettingState(
    key = key,
    values = values,
    defaultValue = default,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current)
)

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

@Composable
fun fractionPreferenceState(
    key: String,
    denominator: Int,
    defaultNumerator: Int
) = rememberFractionPreferenceSettingState(
    key = key,
    denominator = denominator,
    defaultNumerator = defaultNumerator,
    preferences = SharedPreferencesHelper.getSharedPreferences(LocalContext.current)
)
