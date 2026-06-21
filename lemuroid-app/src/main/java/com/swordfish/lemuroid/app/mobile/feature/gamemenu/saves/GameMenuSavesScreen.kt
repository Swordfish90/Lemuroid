package com.swordfish.lemuroid.app.mobile.feature.gamemenu.saves

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GameMenuSavesScreen(
    viewModel: GameMenuSavesViewModel,
    game: Game,
) {
    val transferState by viewModel.transferState.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { viewModel.exportSaves(it) }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { viewModel.importSaves(it) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LemuroidSettingsMenuLink(
            title = { Text(stringResource(R.string.game_menu_saves_export)) },
            onClick = {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/zip"
                    putExtra(Intent.EXTRA_TITLE, "${game.title} saves.zip")
                }
                exportLauncher.launch(intent)
            },
        )

        LemuroidSettingsMenuLink(
            title = { Text(stringResource(R.string.game_menu_saves_import)) },
            onClick = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/zip"
                }
                importLauncher.launch(intent)
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (val state = transferState) {
                is GameMenuSavesViewModel.TransferState.Idle -> {}
                is GameMenuSavesViewModel.TransferState.Working -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.game_menu_saves_working),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                is GameMenuSavesViewModel.TransferState.Success -> {
                    Text(
                        text = stringResource(R.string.game_menu_saves_success, state.message.toIntOrNull() ?: 0),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                is GameMenuSavesViewModel.TransferState.Error -> {
                    Text(
                        text = stringResource(R.string.game_menu_saves_error, state.message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
