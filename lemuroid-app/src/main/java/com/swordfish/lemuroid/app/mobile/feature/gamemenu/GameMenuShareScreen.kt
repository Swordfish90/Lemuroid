package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.roomcord.ShareRoomcordTextGenerator
import com.swordfish.lemuroid.app.appextension.roomcord.ShareSuccessDialog
import com.swordfish.lemuroid.lib.library.db.entity.Game

private const val PREFS_NAME = "roomcord_share_prefs"
private const val KEY_USER_NAME = "user_name"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameMenuShareScreen(
    game: Game,
    shareGenerator: ShareRoomcordTextGenerator,
    onShareComplete: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }

    val name = remember { mutableStateOf(prefs.getString(KEY_USER_NAME, "") ?: "") }
    val feedback = remember { mutableStateOf("") }
    val showSuccessDialog = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.share_discord_popup_description),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = name.value,
            onValueChange = { name.value = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(id = R.string.share_discord_dialog_enter_name)) },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = feedback.value,
            onValueChange = { feedback.value = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(id = R.string.share_discord_dialog_enter_feedback)) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value,
            onClick = {
                when {
                    name.value.isBlank() -> Toast.makeText(context, "Enter your name!", Toast.LENGTH_SHORT).show()
                    else -> {
                        prefs.edit().putString(KEY_USER_NAME, name.value.trim()).apply()

                        val shareTextPart1 = context.getString(
                            R.string.share_discord_text_title_part_1,
                            name.value.trim(),
                            game.title
                        )
                        val content = if (feedback.value.isNotBlank()) "$shareTextPart1 ${feedback.value.trim()}" else shareTextPart1

                        isLoading.value = true
                        shareGenerator.shareGame(
                            game = game,
                            content = content,
                            onSuccess = {
                                isLoading.value = false
                                showSuccessDialog.value = true
                            },
                            onError = { msg ->
                                isLoading.value = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = stringResource(id = R.string.share_discord_button_title))
            }
        }
    }

    if (showSuccessDialog.value) {
        ShareSuccessDialog(onDismiss = {
            showSuccessDialog.value = false
            onShareComplete()
        })
    }
}
