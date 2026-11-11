package com.swordfish.lemuroid.app.mobile.feature.editgame

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidTopAppBar
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.LemuroidSearchView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSmallGameImage
import com.swordfish.lemuroid.app.mobile.shared.compose.utils.rememberSlideAwayTopBarScrollBehavior

@Composable
fun EditGameScreen(viewModel: EditGameViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    BackHandler { if (!state.busy) onDismiss() }

    AnimatedContent(targetState = state.busy, label = "EditGameContent") { busy ->
        if (busy) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            EditGameScreenContent(
                onDismiss = onDismiss,
                viewModel = viewModel,
                state = state,
            )
        }
    }

    if (state.choosingGame) {
        DatabaseGamePickerDialog(viewModel)
    }
}

@Composable
private fun EditGameScreenContent(
    onDismiss: () -> Unit,
    viewModel: EditGameViewModel,
    state: EditGameViewModel.UiState,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.pickImage(it.toString()) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LemuroidTopAppBar(
                title = { Text(stringResource(R.string.game_context_menu_edit)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(onDismiss) },
                        enabled = state.draft.title.isNotBlank()
                    ) {
                        Text(stringResource(R.string.game_edit_save))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LemuroidSmallGameImage(
                modifier = Modifier
                    .size(160.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.game_edit_pick_artwork),
                        onClick = { imagePicker.launch("image/*") },
                    ),
                game = state.draft.copy(coverFrontUrl = state.pickedImage ?: state.draft.coverFrontUrl),
            )
            OutlinedTextField(
                value = state.draft.title,
                onValueChange = viewModel::changeTitle,
                label = { Text(stringResource(R.string.game_edit_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.showDatabase(true) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.game_edit_database))
            }
            TextButton(
                onClick = viewModel::restoreDefaults,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.game_edit_restore))
            }
        }
    }
}

@Composable
private fun DatabaseGamePickerDialog(viewModel: EditGameViewModel) {
    val query by viewModel.query.collectAsState()
    val results = viewModel.candidates.collectAsLazyPagingItems()
    val onDismiss = { viewModel.showDatabase(false) }

    val scrollBehavior = rememberSlideAwayTopBarScrollBehavior()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LemuroidTopAppBar(
                    title = { LemuroidSearchView(query, viewModel::changeQuery) },
                    modifier = with(scrollBehavior) { Modifier.slideAwayTopBar() },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(
                    count = results.itemCount, key = results.itemKey { it.id }) { index ->
                    val candidate = results[index] ?: return@items
                    LemuroidGameListRow(
                        game = candidate,
                        onClick = { viewModel.chooseGame(candidate) },
                    )
                }
            }
        }
    }
}
