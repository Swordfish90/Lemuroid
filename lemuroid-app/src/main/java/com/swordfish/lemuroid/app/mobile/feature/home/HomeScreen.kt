package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.app.utils.games.PlayTimeFormatter
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.CollectionWithGames
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameCollection
import java.util.Locale

private val STARTER_COLLECTION_RESOURCES = listOf(
    R.string.starter_collection_playing,
    R.string.starter_collection_completed,
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGamePlay: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
    onOpenCoreSelection: () -> Unit,
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.updatePermissions(applicationContext)
            }
            else -> { }
        }
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (!isGranted) {
                context.displayDetailsSettingsScreen()
            }
        }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState())
    HomeScreenContent(
        modifier = modifier,
        state = state.value,
        onGameClicked = onGameClick,
        onGamePlay = onGamePlay,
        onGameLongClick = onGameLongClick,
        onGameFavoriteToggle = onGameFavoriteToggle,
        onOpenCoreSelection = onOpenCoreSelection,
        onEnableNotificationsClicked = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return@HomeScreenContent
            }
            permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        onEnableMicrophoneClicked = { permissionsLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onSetDirectoryClicked = { viewModel.changeLocalStorageFolder(context) },
        onCreateCollection = { viewModel.createCollection(it) },
        onRenameCollection = { col, newName -> viewModel.renameCollection(col, newName) },
        onDeleteCollection = { viewModel.deleteCollection(it) },
        onShuffleTopRated = { viewModel.shuffleTopRated() },
        onShuffleDiscovery = { viewModel.shuffleDiscovery() },
    )
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeViewModel.UIState,
    onGameClicked: (Game) -> Unit,
    onGamePlay: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onEnableMicrophoneClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
    onCreateCollection: (String) -> Unit,
    onRenameCollection: (GameCollection, String) -> Unit,
    onDeleteCollection: (GameCollection) -> Unit,
    onShuffleTopRated: () -> Unit,
    onShuffleDiscovery: () -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showLibraryStatsDialog by remember { mutableStateOf(false) }
    var collectionToRename by remember { mutableStateOf<GameCollection?>(null) }
    var collectionToDelete by remember { mutableStateOf<GameCollection?>(null) }

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(state.showNoNotificationPermissionCard) {
            HomeNotification(
                titleId = R.string.home_notification_title,
                messageId = R.string.home_notification_message,
                actionId = R.string.home_notification_action,
                onAction = onEnableNotificationsClicked,
            )
        }
        AnimatedVisibility(state.showNoGamesCard) {
            HomeNotification(
                titleId = R.string.home_empty_title,
                messageId = R.string.home_empty_message,
                actionId = R.string.home_empty_action,
                onAction = onSetDirectoryClicked,
                enabled = !state.indexInProgress,
            )
        }
        AnimatedVisibility(state.showNoMicrophonePermissionCard) {
            HomeNotification(
                titleId = R.string.home_microphone_title,
                messageId = R.string.home_microphone_message,
                actionId = R.string.home_microphone_action,
                onAction = onEnableMicrophoneClicked,
            )
        }
        AnimatedVisibility(state.showDesmumeDeprecatedCard) {
            HomeNotification(
                titleId = R.string.home_notification_desmume_deprecated_title,
                messageId = R.string.home_notification_desmume_deprecated_message,
                actionId = R.string.home_notification_desmume_deprecated_action,
                onAction = onOpenCoreSelection,
            )
        }

        state.continuePlayingGame?.let { heroGame ->
            ContinuePlayingHeroCard(
                game = heroGame,
                rating = state.ratingsByGameId[heroGame.id],
                onClick = { onGameClicked(heroGame) },
                onPlay = { onGamePlay(heroGame) },
                onLongClick = { onGameLongClick(heroGame) },
            )
        }

        HomeRow(
            title = stringResource(id = R.string.recent),
            icon = Icons.Default.History,
            games = state.recentGames,
            ratingsByGameId = state.ratingsByGameId,
            onGameClicked = onGameClicked,
            onGameLongClick = onGameLongClick,
            onGameFavoriteToggle = onGameFavoriteToggle,
        )

        if (!state.showNoGamesCard && state.collections.isNotEmpty()) {
            CollectionsSection(
                collections = state.collections,
                ratingsByGameId = state.ratingsByGameId,
                onNewCollectionClick = { showCreateDialog = true },
                onQuickCreateCollection = onCreateCollection,
                onRenameCollection = { collectionToRename = it },
                onDeleteCollection = { collectionToDelete = it },
                onGameClicked = onGameClicked,
                onGameLongClick = onGameLongClick,
                onGameFavoriteToggle = onGameFavoriteToggle,
            )
        }

        HomeRow(
            title = stringResource(id = R.string.favorites),
            icon = Icons.Default.Favorite,
            games = state.favoritesGames,
            ratingsByGameId = state.ratingsByGameId,
            onGameClicked = onGameClicked,
            onGameLongClick = onGameLongClick,
            onGameFavoriteToggle = onGameFavoriteToggle,
        )

        HomeRow(
            title = stringResource(id = R.string.home_top_rated),
            icon = Icons.Default.Star,
            iconTint = Color(0xFFFFC107),
            games = state.topRatedGames,
            ratingsByGameId = state.ratingsByGameId,
            onGameClicked = onGameClicked,
            onGameLongClick = onGameLongClick,
            onGameFavoriteToggle = onGameFavoriteToggle,
            trailingAction = {
                IconButton(
                    onClick = onShuffleTopRated,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(id = R.string.home_top_rated),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
        )

        HomeRow(
            title = stringResource(id = R.string.discover),
            icon = Icons.Default.Explore,
            games = state.discoveryGames,
            ratingsByGameId = state.ratingsByGameId,
            onGameClicked = onGameClicked,
            onGameLongClick = onGameLongClick,
            onGameFavoriteToggle = onGameFavoriteToggle,
            trailingAction = {
                IconButton(
                    onClick = onShuffleDiscovery,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(id = R.string.discover),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
        )

        if (!state.showNoGamesCard && state.collections.isEmpty()) {
            CollectionsSection(
                collections = state.collections,
                ratingsByGameId = state.ratingsByGameId,
                onNewCollectionClick = { showCreateDialog = true },
                onQuickCreateCollection = onCreateCollection,
                onRenameCollection = { collectionToRename = it },
                onDeleteCollection = { collectionToDelete = it },
                onGameClicked = onGameClicked,
                onGameLongClick = onGameLongClick,
                onGameFavoriteToggle = onGameFavoriteToggle,
            )
        }

        if (!state.showNoGamesCard && state.libraryStats.totalGames > 0) {
            LibraryStatsSummaryCard(
                stats = state.libraryStats,
                onClick = { showLibraryStatsDialog = true },
            )
        }
    }

    if (showLibraryStatsDialog) {
        LibraryStatsDialog(
            stats = state.libraryStats,
            onDismiss = { showLibraryStatsDialog = false },
        )
    }

    if (showCreateDialog) {
        CollectionNameDialog(
            title = stringResource(R.string.collection_create_title),
            confirmLabel = stringResource(R.string.collection_create_action),
            initialName = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreateCollection(name)
                showCreateDialog = false
            },
        )
    }

    collectionToRename?.let { collection ->
        CollectionNameDialog(
            title = stringResource(R.string.collection_rename_title),
            confirmLabel = stringResource(R.string.collection_rename_action),
            initialName = collection.name,
            onDismiss = { collectionToRename = null },
            onConfirm = { newName ->
                onRenameCollection(collection, newName)
                collectionToRename = null
            },
        )
    }

    collectionToDelete?.let { collection ->
        AlertDialog(
            onDismissRequest = { collectionToDelete = null },
            title = { Text(text = stringResource(R.string.collection_delete_title)) },
            text = { Text(text = stringResource(R.string.collection_delete_message, collection.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCollection(collection)
                        collectionToDelete = null
                    },
                ) {
                    Text(
                        text = stringResource(R.string.dialog_delete_rom_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { collectionToDelete = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinuePlayingHeroCard(
    game: Game,
    rating: Float? = null,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val fallbackDrawable = remember(game) { CoverUtils.getFallbackDrawable(game) }
    val fallbackPainter = rememberDrawablePainter(drawable = fallbackDrawable)
    val coverUrl = game.displayCoverUrl()
    val relativePlayed = remember(game.lastPlayedAt) {
        game.lastPlayedAt?.takeIf { it > 0L }?.let { ts ->
            DateUtils.getRelativeTimeSpanString(
                ts,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE,
            ).toString()
        }
    }
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        ) {
       
            if (!coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.55f),
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.48f),
                                Color.Black.copy(alpha = 0.68f),
                                Color.Black.copy(alpha = 0.78f),
                            ),
                        ),
                    ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = game.displayName(),
                    contentScale = ContentScale.Crop,
                    fallback = fallbackPainter,
                    error = fallbackPainter,
                    modifier = Modifier
                        .width(112.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp)),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_continue_playing),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        ) {
                            Text(
                                text = game.systemId.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                            )
                        }
                    }

                    Text(
                        text = game.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val playTimeText = remember(game.totalPlayTimeMs) {
                        if (game.totalPlayTimeMs > 0L) PlayTimeFormatter.format(game.totalPlayTimeMs) else null
                    }
                    val metaSubtitle = listOfNotNull(
                        relativePlayed,
                        playTimeText,
                    ).joinToString(" • ")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (rating != null && rating > 0f) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", rating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }

                        if (metaSubtitle.isNotEmpty()) {
                            Text(
                                text = if (rating != null && rating > 0f) " • $metaSubtitle" else metaSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onPlay,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.game_context_menu_resume),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        FilledTonalButton(
                            onClick = onClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.home_continue_details),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionsSection(
    collections: List<CollectionWithGames>,
    ratingsByGameId: Map<Int, Float>,
    onNewCollectionClick: () -> Unit,
    onQuickCreateCollection: (String) -> Unit,
    onRenameCollection: (GameCollection) -> Unit,
    onDeleteCollection: (GameCollection) -> Unit,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.home_collections),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            FilledTonalButton(
                onClick = onNewCollectionClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.home_collections_new),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (collections.isEmpty()) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_collections_empty_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.home_collections_empty_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(STARTER_COLLECTION_RESOURCES) { nameRes ->
                            val starterName = stringResource(nameRes)
                            SuggestionChip(
                                onClick = { onQuickCreateCollection(starterName) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                label = { Text(text = starterName) },
                            )
                        }
                    }
                }
            }
        } else {
            collections.forEach { collectionWithGames ->
                CollectionShelf(
                    collectionWithGames = collectionWithGames,
                    ratingsByGameId = ratingsByGameId,
                    onRename = { onRenameCollection(collectionWithGames.collection) },
                    onDelete = { onDeleteCollection(collectionWithGames.collection) },
                    onGameClicked = onGameClicked,
                    onGameLongClick = onGameLongClick,
                    onGameFavoriteToggle = onGameFavoriteToggle,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectionShelf(
    collectionWithGames: CollectionWithGames,
    ratingsByGameId: Map<Int, Float>,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = collectionWithGames.collection.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = collectionWithGames.games.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.collection_rename_action)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.dialog_delete_rom_confirm),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }

        if (collectionWithGames.games.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Text(
                    text = stringResource(R.string.home_collection_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 2.dp),
            ) {
                items(
                    count = collectionWithGames.games.size,
                    key = { index -> "col_${collectionWithGames.collection.id}_${collectionWithGames.games[index].id}" },
                ) { index ->
                    val game = collectionWithGames.games[index]
                    LemuroidGameCard(
                        modifier = Modifier
                            .widthIn(0.dp, 136.dp)
                            .animateItem(),
                        game = game,
                        rating = ratingsByGameId[game.id],
                        onClick = { onGameClicked(game) },
                        onLongClick = { onGameLongClick(game) },
                        onFavoriteToggle = { isFavorite -> onGameFavoriteToggle(game, isFavorite) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeRow(
    title: String,
    icon: ImageVector,
    games: List<Game>,
    ratingsByGameId: Map<Int, Float>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    if (games.isEmpty()) {
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = if (trailingAction != null) 8.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = games.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
            trailingAction?.invoke()
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
        ) {
            items(games.size, key = { index -> "${games[index].id}_$index" }) { index ->
                val game = games[index]
                LemuroidGameCard(
                    modifier =
                        Modifier
                            .widthIn(0.dp, 136.dp)
                            .animateItem(),
                    game = game,
                    rating = ratingsByGameId[game.id],
                    onClick = { onGameClicked(game) },
                    onLongClick = { onGameLongClick(game) },
                    onFavoriteToggle = { isFavorite -> onGameFavoriteToggle(game, isFavorite) },
                )
            }
        }
    }
}

@Composable
private fun CollectionNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.collection_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.trim().isNotEmpty(),
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun LibraryStatsSummaryCard(
    stats: HomeViewModel.LibraryStats,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val scrapedPercent = if (stats.totalGames > 0) {
        (stats.scrapedGames * 100) / stats.totalGames
    } else 0
    val mediaSizeFormatted = remember(stats.mediaStorageBytes) {
        android.text.format.Formatter.formatShortFileSize(context, stats.mediaStorageBytes)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.home_library_stats),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (stats.mediaStorageBytes > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = mediaSizeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatMetricTile(
                    value = stats.totalGames.toString(),
                    label = stringResource(R.string.title_games),
                    modifier = Modifier.weight(1f),
                )
                StatMetricTile(
                    value = stats.totalSystems.toString(),
                    label = stringResource(R.string.title_systems),
                    modifier = Modifier.weight(1f),
                )
                StatMetricTile(
                    value = stats.playedGames.toString(),
                    label = stringResource(R.string.stats_played),
                    modifier = Modifier.weight(1f),
                )
                StatMetricTile(
                    value = "$scrapedPercent%",
                    label = stringResource(R.string.stats_scraped),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatMetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LibraryStatsDialog(
    stats: HomeViewModel.LibraryStats,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val playedRatio = if (stats.totalGames > 0) {
        stats.playedGames.toFloat() / stats.totalGames.toFloat()
    } else 0f
    val scrapedRatio = if (stats.totalGames > 0) {
        stats.scrapedGames.toFloat() / stats.totalGames.toFloat()
    } else 0f
    val mediaSizeFormatted = remember(stats.mediaStorageBytes) {
        android.text.format.Formatter.formatShortFileSize(context, stats.mediaStorageBytes)
    }
    val maxSystemCount = (stats.systemBreakdown.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(text = stringResource(R.string.home_library_stats))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatMetricTile(
                        value = stats.totalGames.toString(),
                        label = stringResource(R.string.title_games),
                        modifier = Modifier.weight(1f),
                    )
                    StatMetricTile(
                        value = stats.favoriteGames.toString(),
                        label = stringResource(R.string.favorites),
                        modifier = Modifier.weight(1f),
                    )
                    StatMetricTile(
                        value = stats.collectionsCount.toString(),
                        label = stringResource(R.string.home_collections),
                        modifier = Modifier.weight(1f),
                    )
                }

            
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.stats_games_played_progress),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = "${stats.playedGames} / ${stats.totalGames}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { playedRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                }

            
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.stats_scraped_coverage),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = "${stats.scrapedGames} / ${stats.totalGames} (${(scrapedRatio * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { scrapedRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                }

            
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.stats_total_playtime),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = PlayTimeFormatter.format(stats.totalPlayTimeMs),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.stats_media_storage),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.stats_media_files_format,
                            mediaSizeFormatted,
                            stats.mediaFileCount,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (stats.systemBreakdown.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.stats_systems_breakdown),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stats.systemBreakdown.forEach { entry ->
                            val systemLabel = GameSystem.findById(entry.systemId)
                                ?.let { stringResource(it.titleResId) }
                                ?: entry.systemId.uppercase()
                            val fraction = entry.count.toFloat() / maxSystemCount.toFloat()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = systemLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = entry.count.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { fraction.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun HomeNotification(
    titleId: Int,
    messageId: Int,
    actionId: Int,
    enabled: Boolean = true,
    onAction: () -> Unit = { },
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(messageId),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction,
                enabled = enabled,
            ) {
                Text(stringResource(id = actionId))
            }
        }
    }
}
