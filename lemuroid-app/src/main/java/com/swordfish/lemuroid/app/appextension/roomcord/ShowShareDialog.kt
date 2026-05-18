package com.swordfish.lemuroid.app.appextension.roomcord

import android.content.Context
import android.widget.Toast
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.R
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color

private const val PREFS_NAME = "roomcord_share_prefs"
private const val KEY_USER_NAME = "user_name"

@ExperimentalMaterial3Api
@Composable
fun ShowShareDialog(
    game: Game,
    onShare: (Game, String, () -> Unit, (String) -> Unit) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val name = remember { mutableStateOf(prefs.getString(KEY_USER_NAME, "") ?: "") }
    val feedback = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.share_discord_popup_description),
                    color = colorResource(id = R.color.textColorAccent),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp),
                    fontSize = 18.sp
                )

                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        //fftf
//                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
//                        unfocusedIndicatorColor = colorResource(id = R.color.textColorSecondary),
//                        focusedTextColor = colorResource(id = R.color.colorWhite),
//                        containerColor = Color.Transparent
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.share_discord_dialog_enter_name)) },
                )

                TextField(
                    value = feedback.value,
                    onValueChange = { feedback.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        //fftf
//                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
//                        unfocusedIndicatorColor = colorResource(id = R.color.textColorSecondary),
//                        focusedTextColor = colorResource(id = R.color.colorWhite),
//                        containerColor = Color.Transparent
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.share_discord_dialog_enter_feedback)) },
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (name.value.isEmpty()) {
                                Toast.makeText(context, "Enter your name!", Toast.LENGTH_SHORT).show()
                            } else {
                                prefs.edit().putString(KEY_USER_NAME, name.value.trim()).apply()

                                val shareTextPart1 = context.getString(
                                    R.string.share_discord_text_title_part_1,
                                    name.value.trim(),
                                    game.title
                                )

                                val shareText = if (feedback.value.isNotBlank()) "$shareTextPart1 ${feedback.value.trim()}" else shareTextPart1
                                isLoading.value = true
                                onShare(
                                    game,
                                    shareText,
                                    { onDismissRequest() },
                                    { msg ->
                                        isLoading.value = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = !isLoading.value,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 0.dp)
                            .width(150.dp)
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = colorResource(id = R.color.textColorPrimary)
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.share_discord_button_title),
                                color = colorResource(id = R.color.textColorPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}
