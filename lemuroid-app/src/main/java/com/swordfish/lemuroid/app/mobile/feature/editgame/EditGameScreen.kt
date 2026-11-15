package com.swordfish.lemuroid.app.mobile.feature.editgame

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.window.core.layout.WindowSizeClass
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.LemuroidSearchView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.DiscardUnsavedChangesDialog
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSmallGameImage
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidTopAppBar
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.SystemBarProtection
import com.swordfish.lemuroid.app.mobile.shared.compose.utils.rememberSlideAwayTopBarScrollBehavior
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun EditGameScreen(viewModel: EditGameViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) onDismiss()
    }
    BackHandler(enabled = !state.choosingGame) { viewModel.requestDismiss() }

    Box(modifier = Modifier.fillMaxSize()) {
        EditGameScreenContent(
            onDismiss = viewModel::requestDismiss,
            onSave = viewModel::save,
            viewModel = viewModel,
            state = state,
        )
        SystemBarProtection()
    }

    if (state.choosingGame) {
        DatabaseGamePickerDialog(viewModel)
    }

    if (state.confirmingDiscard) {
        DiscardUnsavedChangesDialog(
            onDiscard = viewModel::discardChanges,
            onDismissRequest = viewModel::keepEditing,
        )
    }
}

@OptIn(ExperimentalGridApi::class)
@Composable
private fun EditGameScreenContent(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
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
                loading = state.busy,
                navigationIcon = {
                    IconButton(onClick = onDismiss, enabled = !state.busy) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
                    }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = !state.busy && state.draft.title.isNotBlank()) {
                        Text(stringResource(R.string.game_edit_save))
                    }
                },
            )
        },
    ) { padding ->
        val useTwoColumns = currentWindowAdaptiveInfo().windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

        Grid(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            config = {
                if (useTwoColumns) {
                    column(160.dp)
                }
                column(1.fr)
                gap(16.dp)
            },
        ) {
            EditGameArtwork(
                game = state.draft.copy(coverFrontUrl = state.pickedImage ?: state.draft.coverFrontUrl),
                enabled = !state.busy,
                onPickImage = { imagePicker.launch("image/*") },
                modifier = Modifier.gridItem(alignment = Alignment.TopCenter),
            )
            EditGameDetails(
                game = state.draft,
                enabled = !state.busy,
                onTitleChange = viewModel::changeTitle,
                onDeveloperChange = viewModel::changeDeveloper,
                onChooseDatabase = { viewModel.showDatabase(true) },
                onRestoreDefaults = viewModel::restoreDefaults,
            )
        }
    }
}

@Composable
private fun EditGameArtwork(
    game: Game,
    enabled: Boolean,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LemuroidSmallGameImage(
            modifier = Modifier
                .size(160.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.game_edit_pick_artwork),
                    onClick = onPickImage,
                ),
            game = game,
        )
        TextButton(onClick = onPickImage, enabled = enabled) {
            Text(stringResource(R.string.game_edit_pick_artwork))
        }
    }
}

@Composable
private fun EditGameDetails(
    game: Game,
    enabled: Boolean,
    onTitleChange: (String) -> Unit,
    onDeveloperChange: (String) -> Unit,
    onChooseDatabase: () -> Unit,
    onRestoreDefaults: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = game.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.game_edit_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )
        OutlinedTextField(
            value = game.developer.orEmpty(),
            onValueChange = onDeveloperChange,
            label = { Text(stringResource(R.string.game_edit_developer)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )
        EditGameActions(
            enabled = enabled,
            onChooseDatabase = onChooseDatabase,
            onRestoreDefaults = onRestoreDefaults,
        )
    }
}

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
private fun EditGameActions(
    enabled: Boolean,
    onChooseDatabase: () -> Unit,
    onRestoreDefaults: () -> Unit,
) {
    FlexBox(
        modifier = Modifier.fillMaxWidth(),
        config = {
            wrap(FlexWrap.Wrap)
            gap(8.dp)
        },
    ) {
        FilledTonalButton(
            onClick = onChooseDatabase,
            modifier = Modifier.flex {
                basis(200.dp)
                grow(1f)
            },
            enabled = enabled,
        ) {
            Text(stringResource(R.string.game_edit_database))
        }
        FilledTonalButton(
            onClick = onRestoreDefaults,
            modifier = Modifier.flex {
                basis(200.dp)
                grow(1f)
            },
            enabled = enabled,
        ) {
            Text(stringResource(R.string.game_edit_restore))
        }
    }
}

@Composable
private fun DatabaseGamePickerDialog(viewModel: EditGameViewModel) {
    val query by viewModel.query.collectAsState()
    val results = viewModel.candidates.collectAsLazyPagingItems()
    val onDismiss = { viewModel.showDatabase(false) }

    val scrollBehavior = rememberSlideAwayTopBarScrollBehavior()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
            SystemBarProtection()
        }
    }
}
