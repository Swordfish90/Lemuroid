package come.asg.ominoy.app.mobile.feature.settings.retroachievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import come.asg.ominoy.app.mobile.shared.compose.ui.RetroAchievementsColors as RaColors

/** Stateless editor and saved-key confirmation for RetroAchievements Web API credentials. */
@Composable
fun WebApiKeyDialog(
    username: String,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    hasSavedKey: Boolean,
    isVerifying: Boolean,
    errorMessage: String?,
    isKeyVisible: Boolean,
    onToggleKeyVisibility: () -> Unit,
    onGetApiKey: () -> Unit,
    onSaveAndVerify: () -> Unit,
    onResetAndEnterNewKey: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().imePadding().padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).heightIn(max = maxHeight * 0.92f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = RaColors.Card),
                border = BorderStroke(1.dp, RaColors.Line),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp).clip(CircleShape)
                                .background(if (hasSavedKey) RaColors.Mint.copy(alpha = 0.15f) else RaColors.CardVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = if (hasSavedKey) "API key secured" else "Web API key",
                                tint = if (hasSavedKey) RaColors.Mint else RaColors.Muted,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            "Web API credentials",
                            modifier = Modifier.weight(1f),
                            color = RaColors.Text,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }

                    if (hasSavedKey) {
                        Text("API key is secured", color = RaColors.Mint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Your key is verified before it is saved, stored encrypted on this device, and never shown again after saving.",
                            color = RaColors.Muted,
                            fontSize = 13.sp,
                        )
                        UsernameField(username)
                        OutlinedButton(
                            onClick = onResetAndEnterNewKey,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RaColors.Mint),
                        ) {
                            Text("Reset & enter new key")
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Close")
                        }
                    } else {
                        Text("Connect your account with a Web API key.", color = RaColors.Muted, fontSize = 13.sp)
                        Text(
                            "Your key is verified before it is saved, stored encrypted on this device, and never shown again after saving.",
                            color = RaColors.Muted,
                            fontSize = 13.sp,
                        )
                        UsernameField(username)
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = onApiKeyChange,
                            label = { Text("Web API key") },
                            placeholder = { Text("Paste your key") },
                            singleLine = true,
                            isError = !errorMessage.isNullOrBlank(),
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onToggleKeyVisibility) {
                                    Icon(
                                        if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyVisible) "Hide Web API key" else "Show Web API key",
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (!errorMessage.isNullOrBlank()) {
                            Text(errorMessage, color = RaColors.Error, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onGetApiKey,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RaColors.Mint),
                        ) {
                            Text("Get a Web API key")
                        }
                        Button(
                            onClick = onSaveAndVerify,
                            enabled = apiKey.isNotBlank() && !isVerifying,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RaColors.Mint, contentColor = RaColors.OnMint),
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = RaColors.OnMint, strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                            }
                            Text(if (isVerifying) "Verifying…" else "Save & verify")
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsernameField(username: String) {
    OutlinedTextField(
        value = username,
        onValueChange = {},
        readOnly = true,
        label = { Text("RetroAchievements username") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}
