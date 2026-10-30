package com.swordfish.lemuroid.app.mobile.feature.settings.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.mobile.shared.compose.TouchControlsAppearanceEditor
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Appearance
import kotlinx.coroutines.launch

@Composable
internal fun TouchControlsSettings() {
    val context = LocalContext.current
    val manager =
        remember(context) { TouchControllerSettingsManager(SharedPreferencesHelper.getSharedPreferences(context)) }
    val appearance by remember(manager) { manager.observeAppearance() }.collectAsState(Appearance())
    val scope = rememberCoroutineScope()
    LemuroidCardSettingsGroup(title = { Text(stringResource(R.string.touch_customize_shared_title)) }) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.touch_customize_shared_description))
            TouchControlsAppearanceEditor(appearance, onChange = { scope.launch { manager.storeAppearance(it) } })
            TextButton(onClick = { scope.launch { manager.storeAppearance(Appearance()) } }) {
                Text(stringResource(R.string.touch_customize_reset_shared))
            }
        }
    }
}
