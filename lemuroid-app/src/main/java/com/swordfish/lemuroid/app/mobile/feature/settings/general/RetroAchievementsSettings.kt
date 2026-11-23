package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.retroachievements.RetroAchievementsManager
import com.swordfish.lemuroid.app.shared.retroachievements.RetroAchievementsPreferences
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState

@Composable
fun RetroAchievementsSettings() {
    val context = LocalContext.current
    val raPrefs = remember { RetroAchievementsPreferences(context) }

    val enabledState = booleanPreferenceState(
        key = RetroAchievementsPreferences.KEY_ENABLED,
        default = false,
    )

    val hardcoreState = booleanPreferenceState(
        key = RetroAchievementsPreferences.KEY_HARDCORE,
        default = false,
    )

    var currentUsername by remember { mutableStateOf(raPrefs.username) }
    var currentToken by remember { mutableStateOf(raPrefs.token) }
    val isLoggedIn = currentUsername.isNotEmpty() && currentToken.isNotEmpty()

    var showLoginDialog by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }

    val statusMessage by RetroAchievementsManager.statusMessages.collectAsState(initial = null)

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            isLoggingIn = false
            currentUsername = raPrefs.username
            currentToken = raPrefs.token
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (msg.startsWith("Logged in as")) {
                showLoginDialog = false
            }
        }
    }

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_retroachievements)) },
    ) {
        LemuroidSettingsSwitch(
            state = enabledState,
            title = { Text(text = stringResource(id = R.string.settings_title_retroachievements_enable)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_desc_retroachievements_enable)) },
        )

        if (enabledState.value) {
            if (isLoggedIn) {
                LemuroidSettingsMenuLink(
                    title = { Text(text = stringResource(id = R.string.ra_settings_logged_in)) },
                    subtitle = { Text(text = stringResource(id = R.string.ra_settings_account_format, currentUsername)) },
                    onClick = {},
                )

                LemuroidSettingsMenuLink(
                    title = { Text(text = stringResource(id = R.string.ra_settings_log_out), color = MaterialTheme.colorScheme.error) },
                    subtitle = { Text(text = stringResource(id = R.string.ra_settings_log_out_desc)) },
                    onClick = {
                        raPrefs.username = ""
                        raPrefs.token = ""
                        currentUsername = ""
                        currentToken = ""
                        RetroAchievementsManager.logout(context)
                    },
                )
            } else {
                LemuroidSettingsMenuLink(
                    title = { Text(text = stringResource(id = R.string.ra_settings_log_in)) },
                    subtitle = { Text(text = stringResource(id = R.string.ra_settings_log_in_desc)) },
                    onClick = {
                        tempUsername = currentUsername
                        tempPassword = ""
                        showLoginDialog = true
                    },
                )
            }

            LemuroidSettingsSwitch(
                state = hardcoreState,
                title = { Text(text = stringResource(id = R.string.settings_title_retroachievements_hardcore)) },
                subtitle = { Text(text = stringResource(id = R.string.settings_desc_retroachievements_hardcore)) },
            )
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoggingIn) showLoginDialog = false },
            title = { Text(text = stringResource(id = R.string.ra_dialog_login_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(id = R.string.ra_dialog_login_desc_password),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempUsername,
                        onValueChange = { tempUsername = it },
                        label = { Text(text = stringResource(id = R.string.ra_dialog_username_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoggingIn,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempPassword,
                        onValueChange = { tempPassword = it },
                        label = { Text(text = stringResource(id = R.string.ra_dialog_password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoggingIn,
                    )

                    if (isLoggingIn) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                            Text(text = stringResource(id = R.string.ra_dialog_logging_in), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val u = tempUsername.trim()
                        val p = tempPassword.trim()
                        if (u.isNotEmpty() && p.isNotEmpty()) {
                            isLoggingIn = true
                            RetroAchievementsManager.login(context, u, p, isPassword = true)
                        }
                    },
                    enabled = !isLoggingIn && tempUsername.isNotBlank() && tempPassword.isNotBlank(),
                ) {
                    Text(text = stringResource(id = R.string.ra_dialog_login_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginDialog = false },
                    enabled = !isLoggingIn,
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}
