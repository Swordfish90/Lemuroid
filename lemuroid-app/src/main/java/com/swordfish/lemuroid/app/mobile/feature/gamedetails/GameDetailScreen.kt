package com.swordfish.lemuroid.app.mobile.feature.gamedetails

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.swordfish.lemuroid.app.shared.retroachievements.AchievementsUiState
import com.swordfish.lemuroid.app.shared.retroachievements.RetroAchievementsPreferences
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: GameDetailViewModel,
    onBack: () -> Unit,
    onPlayGame: (Game) -> Unit,
    onOpenCheats: (Game) -> Unit,
    onGenreClick: ((Game, String) -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val achievementsState by viewModel.achievementsState.collectAsState()
    val context = LocalContext.current
    val isHardcore = remember { RetroAchievementsPreferences(context).hardcore }
    val gameWithDetails = state.gameWithDetails
    val game = gameWithDetails?.game

    if (game == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val details = gameWithDetails.details
    var previewLightbox by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    var synopsisExpanded by remember { mutableStateOf(false) }
    var failedUrls by remember(state.screenshotUrls) { mutableStateOf(setOf<String>()) }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 36.dp),
        ) {
           
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            ) {
                val bannerUrl = details?.bannerUrl ?: details?.boxArtFrontUrl ?: game.coverFrontUrl
                if (!bannerUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bannerUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                ),
                            ),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background,
                                ),
                            ),
                        ),
                )

                if (!details?.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(details.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = game.title,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                            .height(60.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Top,
            ) {
            
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(0.72f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    val coverUrl = details?.boxArtFrontUrl ?: game.coverFrontUrl
                    if (!coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = game.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        LemuroidGameImage(
                            modifier = Modifier.fillMaxSize(),
                            game = game,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                ) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val developer = details?.publisher ?: game.developer
                    if (!developer.isNullOrBlank()) {
                        Text(
                            text = developer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    val genresList = details?.genres
                        ?.split(Regex("[,|/•]"))
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?.distinctBy { it.lowercase(Locale.ROOT) }
                        ?: emptyList()

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item(key = "tag_system") {
                            GameDetailTag(text = game.systemId.uppercase(Locale.ROOT))
                        }

                        details?.releaseDate?.take(4)?.let { year ->
                            item(key = "tag_year") {
                                GameDetailTag(text = year)
                            }
                        }

                        details?.rating?.takeIf { it > 0f }?.let { rating ->
                            val count = details.ratingCount?.takeIf { it > 0 }
                            val ratingText = if (count != null) {
                                String.format(Locale.US, "%.1f (%,d)", rating, count)
                            } else {
                                String.format(Locale.US, "%.1f", rating)
                            }
                            item(key = "tag_rating") {
                                GameDetailTag(
                                    text = ratingText,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = stringResource(R.string.content_desc_rating),
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                )
                            }
                        }

                        items(genresList.size, key = { index -> "${genresList[index]}_$index" }) { index ->
                            val genre = genresList[index]
                            GameDetailTag(
                                text = genre,
                                onClick = onGenreClick?.let { handler -> { handler(game, genre) } },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { onPlayGame(game) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.game_details_play),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                OutlinedButton(
                    onClick = { onOpenCheats(game) },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VideogameAsset,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val cheatsText = if (state.cheatCount > 0) {
                        "${stringResource(R.string.game_menu_cheats)} (${state.cheatCount})"
                    } else {
                        stringResource(R.string.game_menu_cheats)
                    }
                    Text(
                        text = cheatsText,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            val validScreenshots = remember(state.screenshotUrls, failedUrls) {
                state.screenshotUrls.distinct().filterNot { it in failedUrls }
            }
            val videoUrl = details?.videoUrl
            val hasMedia = validScreenshots.isNotEmpty() || !videoUrl.isNullOrBlank()

            if (hasMedia) {
                val mediaCount = validScreenshots.size + (if (!videoUrl.isNullOrBlank()) 1 else 0)
                val mediaTitle = if (!videoUrl.isNullOrBlank()) {
                    stringResource(R.string.game_details_media_screenshots_video, mediaCount)
                } else {
                    stringResource(R.string.game_details_media_screenshots, mediaCount)
                }

                Text(
                    text = mediaTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    if (!videoUrl.isNullOrBlank()) {
                        item(key = "game_video_preview") {
                            val thumbnailBackdrop = validScreenshots.firstOrNull()
                                ?: details?.bannerUrl
                                ?: details?.boxArtFrontUrl
                                ?: game.coverFrontUrl
                            InlineGameplayVideoCard(
                                videoUrl = videoUrl,
                                thumbnailUrl = thumbnailBackdrop,
                            )
                        }
                    }

                    items(validScreenshots.size, key = { index -> "${validScreenshots[index]}_$index" }) { index ->
                        val url = validScreenshots[index]
                        Card(
                            modifier = Modifier
                                .width(240.dp)
                                .aspectRatio(16f / 9f)
                                .clickable { previewLightbox = validScreenshots to index },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(R.string.content_desc_screenshot),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = {
                                    failedUrls = failedUrls + url
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (state.isScraping) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.game_details_loading_media),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val synopsis = details?.synopsis
            if (!synopsis.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.game_details_about),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = synopsis,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3,
                            maxLines = if (synopsisExpanded) Int.MAX_VALUE else 5,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize(),
                        )
                        if (synopsis.length > 200) {
                            TextButton(
                                onClick = { synopsisExpanded = !synopsisExpanded },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text(
                                    text = if (synopsisExpanded) {
                                        stringResource(R.string.game_details_read_less)
                                    } else {
                                        stringResource(R.string.game_details_read_more)
                                    },
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            GameDetailAchievementsSection(
                achievementsState = achievementsState,
                isHardcore = isHardcore,
                onRefresh = { viewModel.refreshAchievements() },
            )
            if (achievementsState is AchievementsUiState.Success || achievementsState is AchievementsUiState.Loading || achievementsState is AchievementsUiState.Error) {
                Spacer(modifier = Modifier.height(20.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.game_details_file_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val displayFileName = state.actualFileName?.takeIf { it != game.fileName }
                        ?.let { "$it (${game.fileName})" }
                        ?: game.fileName
                    DetailInfoRow(
                        label = stringResource(R.string.game_details_filename),
                        value = displayFileName,
                    )
                    val fullSystemName = stringResource(GameSystem.findById(game.systemId).titleResId)
                    val shortSystemId = game.systemId.uppercase(Locale.ROOT)
                    val displaySystemName = if (fullSystemName.equals(shortSystemId, ignoreCase = true)) {
                        fullSystemName
                    } else {
                        "$fullSystemName ($shortSystemId)"
                    }
                    DetailInfoRow(
                        label = stringResource(R.string.game_details_system),
                        value = displaySystemName,
                    )
                    state.fileSizeFormatted?.takeIf { it.isNotBlank() }?.let { size ->
                        DetailInfoRow(
                            label = stringResource(R.string.game_details_rom_size),
                            value = size,
                        )
                    }
                    state.crc32?.takeIf { it.isNotBlank() }?.let { crc32 ->
                        DetailInfoRow(
                            label = stringResource(R.string.game_details_crc32),
                            value = crc32,
                        )
                    }
                    val lastPlayedAt = game.lastPlayedAt
                    val lastPlayedText = if (lastPlayedAt != null && lastPlayedAt > 0L) {
                        java.text.DateFormat.getDateTimeInstance(
                            java.text.DateFormat.MEDIUM,
                            java.text.DateFormat.SHORT,
                        ).format(java.util.Date(lastPlayedAt))
                    } else {
                        stringResource(R.string.game_details_last_played_never)
                    }
                    DetailInfoRow(
                        label = stringResource(R.string.game_details_last_played),
                        value = lastPlayedText,
                    )
                    if (game.totalPlayTimeMs > 0L) {
                        DetailInfoRow(
                            label = stringResource(R.string.game_details_playtime),
                            value = com.swordfish.lemuroid.app.utils.games.PlayTimeFormatter.format(game.totalPlayTimeMs),
                        )
                    }
                    if (!details?.scraperSource.isNullOrBlank()) {
                        DetailInfoRow(
                            label = stringResource(R.string.game_details_source),
                            value = details?.scraperSource.orEmpty(),
                        )
                    }
                    val lastScrapedAt = details?.lastScrapedAt
                    if (lastScrapedAt != null && lastScrapedAt > 0) {
                        DetailInfoRow(
                            label = stringResource(R.string.game_details_last_updated),
                            value = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(java.util.Date(lastScrapedAt)),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                ) {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.content_desc_favorite),
                            tint = if (game.isFavorite) Color.Red else Color.White,
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                ) {
                    IconButton(
                        onClick = {
                            if (state.isScraping) {
                                viewModel.cancelScrape()
                            } else {
                                viewModel.rescrape()
                            }
                        },
                    ) {
                        if (state.isScraping) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(24.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.content_desc_cancel_scraping),
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.content_desc_refresh_metadata),
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }

        previewLightbox?.let { (images, initialIndex) ->
            if (images.isNotEmpty()) {
                BackHandler {
                    previewLightbox = null
                }

                val safeInitial = initialIndex.coerceIn(0, images.lastIndex)
                val pagerState = rememberPagerState(
                    initialPage = safeInitial,
                    pageCount = { images.size },
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(images[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.content_desc_screenshot_counter, page + 1, images.size),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(16.dp),
                    ) {
                        IconButton(onClick = { previewLightbox = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = Color.White,
                            )
                        }
                    }

                    if (images.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 24.dp),
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${images.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun InlineGameplayVideoCard(
    videoUrl: String,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    val context = LocalContext.current

    DisposableEffect(isPlaying) {
        onDispose {
            videoViewRef?.stopPlayback()
            mediaPlayerRef = null
        }
    }

    Card(
        modifier = modifier
            .width(240.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {

            if (!isPlaying && !thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.settings_media_source_video),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            if (isPlaying && !hasError) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            videoViewRef = this
                            if (videoUrl.startsWith("file://")) {
                                val path = Uri.parse(videoUrl).path ?: videoUrl.removePrefix("file://")
                                setVideoPath(path)
                            } else if (videoUrl.startsWith("/")) {
                                setVideoPath(videoUrl)
                            } else {
                                val headers = mapOf("User-Agent" to "User-Agent")
                                setVideoURI(Uri.parse(videoUrl), headers)
                            }
                            setOnPreparedListener { mp ->
                                mediaPlayerRef = mp
                                isBuffering = false
                                mp.isLooping = true
                                val vol = if (isMuted) 0f else 1f
                                mp.setVolume(vol, vol)
                                start()
                            }
                            setOnErrorListener { _, what, extra ->
                                android.util.Log.e("InlineVideo", "VideoView error: what=$what extra=$extra")
                                isBuffering = false
                                hasError = true
                                true
                            }
                        }
                    },
                )

                if (isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.dp,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
            
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.size(32.dp),
                    ) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                val vol = if (isMuted) 0f else 1f
                                mediaPlayerRef?.setVolume(vol, vol)
                            },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isMuted) stringResource(R.string.content_desc_unmute) else stringResource(R.string.game_menu_mute_audio),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.size(32.dp),
                    ) {
                        IconButton(
                            onClick = {
                                videoViewRef?.stopPlayback()
                                isPlaying = false
                                isBuffering = false
                            },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.stop),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.game_details_video_playing),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            } else {
            
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            hasError = false
                            isBuffering = true
                            isPlaying = true
                        },
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        shadowElevation = 8.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.content_desc_play_video),
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                    ) {
                        Text(
                            text = if (hasError) stringResource(R.string.game_details_video_retry) else stringResource(R.string.game_details_video_gameplay),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun GameDetailTag(
    text: String,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
