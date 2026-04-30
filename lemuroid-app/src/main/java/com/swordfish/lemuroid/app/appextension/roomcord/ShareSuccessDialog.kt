package com.swordfish.lemuroid.app.appextension.roomcord

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs

@Composable
fun ShareSuccessDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.share_success_title)) },
        text = { Text(text = stringResource(R.string.share_success_message)) },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(FulldiveConfigs.ROOMCORD_ROOM_URL_STORY))
                context.startActivity(intent)
                onDismiss()
            }) {
                Text(text = stringResource(R.string.share_success_view))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.share_success_ok))
            }
        }
    )
}
