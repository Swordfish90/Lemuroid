package com.swordfish.lemuroid.app.mobile.feature.gamedetails

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import java.io.File
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.common.kotlin.calculateRomCrc32
import com.swordfish.lemuroid.lib.cheats.cht.ChtDiscoveryManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameDetails
import com.swordfish.lemuroid.lib.library.db.entity.GameWithDetails
import com.swordfish.lemuroid.lib.scraper.AlphaLemuroidClient
import com.swordfish.lemuroid.lib.scraper.MediaStorageManager
import com.swordfish.lemuroid.lib.scraper.ScreenScraperClient
import com.swordfish.lemuroid.lib.scraper.ScreenScraperSystemMapper
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.swordfish.lemuroid.app.shared.retroachievements.AchievementsUiState
import com.swordfish.lemuroid.app.shared.retroachievements.RetroAchievementsRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class GameDetailViewModel(
    val gameId: Int,
    private val retrogradeDb: RetrogradeDatabase,
    private val settingsManager: SettingsManager,
    private val appContext: Context,
    private val screenScraperClient: ScreenScraperClient = ScreenScraperClient(),
    private val alphaLemuroidClient: AlphaLemuroidClient = AlphaLemuroidClient(),
    private val mediaStorageManager: MediaStorageManager = MediaStorageManager(appContext),
) : ViewModel() {

    data class FileMetadata(
        val actualFileName: String? = null,
        val fileSizeFormatted: String? = null,
        val crc32: String? = null,
    )

    data class UiState(
        val gameWithDetails: GameWithDetails? = null,
        val cheatCount: Int = 0,
        val isScraping: Boolean = false,
        val screenshotUrls: List<String> = emptyList(),
        val crc32: String? = null,
        val actualFileName: String? = null,
        val fileSizeFormatted: String? = null,
    )

    private val retroAchievementsRepository = RetroAchievementsRepository(appContext)
    private val _achievementsState = MutableStateFlow<AchievementsUiState>(AchievementsUiState.Disabled)
    val achievementsState: StateFlow<AchievementsUiState> = _achievementsState.asStateFlow()
    private var achievementsJob: Job? = null

    private val libretroDbManager by lazy { LibretroDBManager(appContext) }
    private val isScrapingFlow = MutableStateFlow(false)
    private val fileMetadataFlow = MutableStateFlow(FileMetadata())
    private var hasAttemptedInitialScrape = false
    private var scrapeJob: Job? = null

    private val gameWithDetailsFlow = retrogradeDb.gameDetailsDao()
        .getGameWithDetails(gameId)

    private val cheatCountFlow = retrogradeDb.cheatDao()
        .getCheatsForGame(gameId)

    val uiState: StateFlow<UiState> = combine(
        gameWithDetailsFlow,
        cheatCountFlow,
        isScrapingFlow,
        fileMetadataFlow,
    ) { gameWithDetails, cheats, isScraping, fileMeta ->
        val screenshots = gameWithDetails?.details?.screenshotUrlsJson?.let {
            screenScraperClient.deserializeScreenshots(it)
        } ?: emptyList()

        UiState(
            gameWithDetails = gameWithDetails,
            cheatCount = cheats.size,
            isScraping = isScraping,
            screenshotUrls = screenshots,
            crc32 = fileMeta.crc32,
            actualFileName = fileMeta.actualFileName,
            fileSizeFormatted = fileMeta.fileSizeFormatted,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(isScraping = false),
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val game = gameWithDetailsFlow.first()?.game ?: return@launch
            runCatching {
                ChtDiscoveryManager.syncDiscoveredCheats(appContext, game, retrogradeDb.cheatDao())
            }.onFailure {
                timber.log.Timber.e(it, "Failed to auto-discover cheats in GameDetailViewModel for ${game.id}")
            }
            val uri = runCatching { Uri.parse(game.fileUri) }.getOrNull()
            if (uri != null) {
                val docFile = if (uri.scheme == "content") {
                    runCatching { DocumentFile.fromSingleUri(appContext, uri) }.getOrNull()
                } else null

                val diskName = when (uri.scheme) {
                    "content" -> docFile?.name
                    "file", null -> uri.path?.let { File(it).name }
                    else -> null
                }?.takeIf { it.isNotBlank() }

                val diskSizeBytes = when (uri.scheme) {
                    "content" -> {
                        val docLen = docFile?.length()?.takeIf { it > 0L }
                        docLen ?: runCatching {
                            appContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                                pfd.statSize.takeIf { it > 0L }
                            }
                        }.getOrNull()
                    }
                    "file", null -> uri.path?.let { File(it).length().takeIf { len -> len > 0L } }
                    else -> null
                }
                val formattedSize = diskSizeBytes?.let {
                    android.text.format.Formatter.formatShortFileSize(appContext, it)
                }

                fileMetadataFlow.value = fileMetadataFlow.value.copy(
                    actualFileName = diskName,
                    fileSizeFormatted = formattedSize,
                )

                val isArcadeZip = game.systemId.equals("fbneo", ignoreCase = true) ||
                    game.systemId.equals("mame2003plus", ignoreCase = true)
                val computedCrc = runCatching {
                    appContext.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.calculateRomCrc32(
                            fileName = diskName ?: game.fileName,
                            isArcadeZip = isArcadeZip,
                        ).uppercase()
                    }
                }.getOrNull()?.takeIf { it.isNotBlank() && it != "0" }
                if (computedCrc != null) {
                    fileMetadataFlow.value = fileMetadataFlow.value.copy(crc32 = computedCrc)
                    val existing = retrogradeDb.gameDetailsDao().getGameDetailsDirect(game.id)
                    if (existing != null && existing.crc32 != computedCrc) {
                        retrogradeDb.gameDetailsDao().updateCrc32(game.id, computedCrc)
                    }
                }
            }
        }

        scrapeJob = viewModelScope.launch {
            val gameWithDetails = gameWithDetailsFlow.first()
            val details = gameWithDetails?.details
            val isNeverScraped = details == null || details.lastScrapedAt == 0L
            if (gameWithDetails != null && isNeverScraped && !hasAttemptedInitialScrape) {
                hasAttemptedInitialScrape = true
                scrapeGame(gameWithDetails.game, force = false)
            }
        }
        achievementsJob = viewModelScope.launch(Dispatchers.IO) {
            val game = gameWithDetailsFlow.first()?.game ?: return@launch
            val crc = fileMetadataFlow.first().crc32
            retroAchievementsRepository.getAchievementsFlow(game, crc).collect { state ->
                _achievementsState.value = state
            }
        }
    }

    fun refreshAchievements() {
        achievementsJob?.cancel()
        achievementsJob = viewModelScope.launch(Dispatchers.IO) {
            val game = gameWithDetailsFlow.first()?.game ?: return@launch
            val crc = fileMetadataFlow.value.crc32
            retroAchievementsRepository.getAchievementsFlow(game, crc).collect { state ->
                _achievementsState.value = state
            }
        }
    }

    fun rescrape() {
        val game = uiState.value.gameWithDetails?.game ?: return
        scrapeJob?.cancel()
        scrapeJob = viewModelScope.launch {
            scrapeGame(game, force = true)
        }
    }

    fun cancelScrape() {
        scrapeJob?.cancel()
        scrapeJob = null
        isScrapingFlow.value = false
    }

    fun toggleFavorite() {
        val game = uiState.value.gameWithDetails?.game ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = game.copy(isFavorite = !game.isFavorite)
            retrogradeDb.gameDao().update(listOf(updated))
        }
    }

    private suspend fun scrapeGame(game: Game, force: Boolean) = withContext(Dispatchers.IO) {
        if (isScrapingFlow.value) return@withContext
        isScrapingFlow.value = true
        android.util.Log.i("GameDetailViewModel", "scrapeGame triggered for gameId=${game.id}, title=${game.title}, file=${game.fileName}")

        try {
            val existingDetails = retrogradeDb.gameDetailsDao().getGameDetailsDirect(game.id)
            val existingBanner = existingDetails?.bannerUrl?.takeIf { mediaStorageManager.isMediaUriValid(it) }
            val existingLogo = existingDetails?.logoUrl?.takeIf { mediaStorageManager.isMediaUriValid(it) }
            val existingBoxArt = existingDetails?.boxArtFrontUrl?.takeIf { mediaStorageManager.isMediaUriValid(it) }
            val existingBackBoxArt = existingDetails?.boxArtBackUrl?.takeIf { mediaStorageManager.isMediaUriValid(it) }
            val existingVideo = existingDetails?.videoUrl?.takeIf { mediaStorageManager.isMediaUriValid(it) }
            val existingScreenshots = existingDetails?.screenshotUrlsJson
                ?.let { screenScraperClient.deserializeScreenshots(it) }
                ?.filter { mediaStorageManager.isMediaUriValid(it) }
                ?: emptyList()

            val uri = runCatching { Uri.parse(game.fileUri) }.getOrNull()
            var crc: String? = null
            var fileSize: Long? = null

            val isArcadeZip = game.systemId.equals("fbneo", ignoreCase = true) ||
                game.systemId.equals("mame2003plus", ignoreCase = true)

            if (uri != null) {
                runCatching {
                    appContext.contentResolver.openInputStream(uri)?.use { stream ->
                        crc = stream.calculateRomCrc32(
                            fileName = game.fileName,
                            isArcadeZip = isArcadeZip,
                        )
                    }
                }
                runCatching {
                    appContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        fileSize = pfd.statSize.takeIf { it > 0 }
                    }
                }
            }

            val libretroRom = if (!crc.isNullOrBlank() && crc != "0") {
                runCatching {
                    libretroDbManager.dbInstance.gameDao().findByCRC(crc!!.uppercase())
                }.getOrNull()
            } else null

            val romBasename = game.fileName.substringBeforeLast('.')
            val systemId = game.systemId
            val downloadedUrlMap = ConcurrentHashMap<String, java.io.File>()

            val canonicalTitle = libretroRom?.name?.takeIf { it.isNotBlank() }
                ?: alphaLemuroidClient.cleanGameTitle(game.displayName())

            val systemEid = ScreenScraperSystemMapper.getSystemEid(game.systemId)
            val user = settingsManager.screenScraperUser().takeIf { it.isNotBlank() }
            val pass = settingsManager.screenScraperPassword().takeIf { it.isNotBlank() }

            val boxArtFormat = settingsManager.screenScraperBoxArtFormat()
            val scraperMode = settingsManager.scraperPriority().uppercase()
            val useScreenScraper = scraperMode == "SCREENSCRAPER" || scraperMode == "MIXED"
            val useIgdb = scraperMode == "IGDB" || scraperMode == "MIXED"
            val preferIgdb = scraperMode == "IGDB"
            val mediaSources = settingsManager.screenScraperMediaSources()
            val shouldDownloadBanner = mediaSources.contains("banner")
            val shouldDownloadScreenshots = mediaSources.contains("ss")
            val shouldDownloadTitles = mediaSources.contains("sstitle")
            val shouldDownloadVideo = mediaSources.contains("video")
            val shouldDownloadFanart = mediaSources.contains("fanart")

            val unifiedResult = runCatching {
                alphaLemuroidClient.scrapeUnified(
                    crc32 = crc,
                    title = if (useIgdb) canonicalTitle else null,
                    romName = if (useScreenScraper) game.fileName else null,
                    systemEid = if (useScreenScraper) systemEid else null,
                    romSize = if (useScreenScraper) fileSize else null,
                    userId = if (useScreenScraper) user else null,
                    userPassword = if (useScreenScraper) pass else null,
                    source = scraperMode.lowercase(),
                )
            }.getOrNull()

            val screenScraperData = if (useScreenScraper && systemEid != null && unifiedResult?.screenScraperJeu != null) {
                runCatching {
                    screenScraperClient.parseJeuJsonObject(
                        jeu = unifiedResult.screenScraperJeu!!,
                        systemEid = systemEid,
                        boxArtFormat = boxArtFormat,
                        mediaSources = mediaSources,
                    )
                }.getOrNull()
            } else null

            val alphaLemuroidData = if (useIgdb) unifiedResult?.igdb else null

            val rawAccurateTitle = if (preferIgdb) {
                alphaLemuroidData?.name?.takeIf { it.isNotBlank() }
                    ?: screenScraperData?.title?.takeIf { it.isNotBlank() }
            } else {
                screenScraperData?.title?.takeIf { it.isNotBlank() }
                    ?: alphaLemuroidData?.name?.takeIf { it.isNotBlank() }
            } ?: libretroRom?.name?.takeIf { it.isNotBlank() }
                ?: canonicalTitle

            val cleanTitle = rawAccurateTitle
                .replace(Regex("\\s*\\([^)]*\\)"), "")
                .replace(Regex("\\s*\\[[^]]*\\]"), "")
                .trim()
                .ifBlank { rawAccurateTitle }

            val mediaSemaphore = Semaphore(5)
            val rawBoxArtUrl = if (preferIgdb) {
                alphaLemuroidData?.coverUrl ?: screenScraperData?.boxArtFrontUrl ?: game.coverFrontUrl
            } else {
                screenScraperData?.boxArtFrontUrl ?: alphaLemuroidData?.coverUrl ?: game.coverFrontUrl
            }
            val rawBannerUrl = if (shouldDownloadBanner) {
                if (preferIgdb) {
                    alphaLemuroidData?.artworks?.firstOrNull() ?: screenScraperData?.bannerUrl
                } else {
                    screenScraperData?.bannerUrl ?: alphaLemuroidData?.artworks?.firstOrNull()
                }
            } else null
            val rawFanartUrl = if (shouldDownloadFanart) {
                if (preferIgdb) {
                    alphaLemuroidData?.artworks?.firstOrNull() ?: screenScraperData?.bannerUrl
                } else {
                    screenScraperData?.bannerUrl ?: alphaLemuroidData?.artworks?.firstOrNull()
                }
            } else null
            val rawLogoUrl = if (shouldDownloadBanner || shouldDownloadFanart) {
                screenScraperData?.logoUrl
            } else null
            val rawVideoUrl = if (shouldDownloadVideo) screenScraperData?.videoUrl else null

            val distinctScreenshots = if (shouldDownloadScreenshots || shouldDownloadTitles) {
                val candidateScreenshots = mutableListOf<String>()
                if (shouldDownloadScreenshots) {
                    val ssOnly = screenScraperData?.screenshotUrls?.filter { !it.contains("title", ignoreCase = true) } ?: emptyList()
                    val igdbShots = alphaLemuroidData?.screenshots ?: emptyList()
                    if (preferIgdb) {
                        candidateScreenshots.addAll(igdbShots)
                        candidateScreenshots.addAll(ssOnly)
                    } else {
                        candidateScreenshots.addAll(ssOnly)
                        candidateScreenshots.addAll(igdbShots)
                    }
                }
                if (shouldDownloadTitles) {
                    val titlesOnly = screenScraperData?.screenshotUrls?.filter { it.contains("title", ignoreCase = true) } ?: emptyList()
                    candidateScreenshots.addAll(titlesOnly)
                }
                candidateScreenshots.distinct().take(10)
            } else emptyList()

            var finalBoxArtUri: String? = null
            var finalBackBoxArtUri: String? = null
            var finalBannerUri: String? = null
            var finalLogoUri: String? = null
            var localScreenshotUris: List<String> = emptyList()
            var finalVideoUri: String? = null

            coroutineScope {
                val boxArtDeferred = async {
                    val local = rawBoxArtUrl?.let {
                        mediaSemaphore.withPermit {
                            mediaStorageManager.saveOrDownloadMedia(it, systemId, MediaStorageManager.MediaType.BOXART, romBasename, downloadedUrlMap)
                        }
                    }
                    mediaStorageManager.toFileUri(local) ?: rawBoxArtUrl
                }

                val bannerDeferred = async {
                    if (shouldDownloadBanner) {
                        val local = rawBannerUrl?.let {
                            mediaSemaphore.withPermit {
                                mediaStorageManager.saveOrDownloadMedia(it, systemId, MediaStorageManager.MediaType.BANNERS, romBasename, downloadedUrlMap)
                            }
                        }
                        mediaStorageManager.toFileUri(local) ?: rawBannerUrl ?: existingBanner
                    } else {
                        existingBanner
                    }
                }

                val fanartDeferred = async {
                    if (shouldDownloadFanart) {
                        rawFanartUrl?.let {
                            mediaSemaphore.withPermit {
                                mediaStorageManager.saveOrDownloadMedia(it, systemId, MediaStorageManager.MediaType.FANART, romBasename, downloadedUrlMap)
                            }
                        }
                    }
                }

                val logoDeferred = async {
                    if (shouldDownloadBanner || shouldDownloadFanart) {
                        val local = rawLogoUrl?.let {
                            mediaSemaphore.withPermit {
                                mediaStorageManager.saveOrDownloadMedia(it, systemId, MediaStorageManager.MediaType.LOGOS, romBasename, downloadedUrlMap)
                            }
                        }
                        mediaStorageManager.toFileUri(local) ?: rawLogoUrl ?: existingLogo
                    } else {
                        existingLogo
                    }
                }

                val screenshotsDeferred = distinctScreenshots.mapIndexed { index, shotUrl ->
                    async {
                        val isTitle = shotUrl.contains("sstitle", ignoreCase = true) || shotUrl.contains("title", ignoreCase = true)
                        val cat = if (isTitle) MediaStorageManager.MediaType.TITLES else MediaStorageManager.MediaType.SCREENSHOTS
                        val localShot = mediaSemaphore.withPermit {
                            mediaStorageManager.saveOrDownloadMedia(shotUrl, systemId, cat, romBasename, downloadedUrlMap, index = index + 1)
                        }
                        mediaStorageManager.toFileUri(localShot) ?: shotUrl
                    }
                }

                val videoDeferred = async {
                    if (shouldDownloadVideo) {
                        val local = rawVideoUrl?.takeIf { it.isNotBlank() }?.let {
                            mediaSemaphore.withPermit {
                                mediaStorageManager.saveOrDownloadMedia(it, systemId, MediaStorageManager.MediaType.VIDEOS, romBasename, downloadedUrlMap)
                            }
                        }
                        mediaStorageManager.toFileUri(local) ?: rawVideoUrl ?: existingVideo
                    } else {
                        existingVideo
                    }
                }

                fanartDeferred.await()
                finalBoxArtUri = boxArtDeferred.await()
                finalBackBoxArtUri = null
                finalBannerUri = bannerDeferred.await()
                finalLogoUri = logoDeferred.await()
                localScreenshotUris = screenshotsDeferred.awaitAll()
                finalVideoUri = videoDeferred.await()
            }
            val mergedScreenshotUris = (localScreenshotUris + existingScreenshots).distinct().take(12)

            ensureActive()
            val hasAnyData = screenScraperData != null || alphaLemuroidData != null || libretroRom != null

            if (hasAnyData) {
                val igdbGenres = listOfNotNull(
                    alphaLemuroidData?.genres?.takeIf { it.isNotEmpty() }?.joinToString(", "),
                    alphaLemuroidData?.themes?.takeIf { it.isNotEmpty() }?.joinToString(", "),
                ).joinToString(", ").takeIf { it.isNotBlank() }
                val ssGenres = screenScraperData?.genres?.takeIf { it.isNotBlank() }
                val effectiveGenres = if (preferIgdb) {
                    igdbGenres ?: ssGenres
                } else {
                    ssGenres ?: igdbGenres
                }

                val igdbSynopsis = alphaLemuroidData?.summary?.takeIf { it.isNotBlank() }
                    ?: alphaLemuroidData?.storyline?.takeIf { it.isNotBlank() }
                val ssSynopsis = screenScraperData?.synopsis?.takeIf { it.isNotBlank() }
                val effectiveSynopsis = if (preferIgdb) {
                    igdbSynopsis ?: ssSynopsis
                } else {
                    ssSynopsis ?: igdbSynopsis
                }

                val igdbPublisher = alphaLemuroidData?.publishers?.firstOrNull()
                    ?: alphaLemuroidData?.developers?.firstOrNull()
                val ssPublisher = screenScraperData?.publisher ?: screenScraperData?.developer
                val effectivePublisher = if (preferIgdb) {
                    igdbPublisher ?: ssPublisher ?: libretroRom?.developer ?: game.developer
                } else {
                    ssPublisher ?: igdbPublisher ?: libretroRom?.developer ?: game.developer
                }

                val effectiveReleaseDate = if (preferIgdb) {
                    alphaLemuroidData?.releaseDate ?: screenScraperData?.releaseDate
                } else {
                    screenScraperData?.releaseDate ?: alphaLemuroidData?.releaseDate
                }

                val effectiveRating = if (preferIgdb) {
                    alphaLemuroidData?.rating?.takeIf { it > 0 } ?: screenScraperData?.rating?.takeIf { it > 0 }
                } else {
                    screenScraperData?.rating?.takeIf { it > 0 } ?: alphaLemuroidData?.rating?.takeIf { it > 0 }
                }

                val effectiveSource = when {
                    screenScraperData != null && alphaLemuroidData != null ->
                        if (preferIgdb) "IGDB + ScreenScraper" else "ScreenScraper + IGDB"
                    alphaLemuroidData != null -> "IGDB"
                    screenScraperData != null -> "ScreenScraper"
                    else -> "Libretro DB"
                }

                val normalizedCrc = crc?.trim()?.uppercase()?.takeIf { it.isNotBlank() && it != "0" }
                    ?: existingDetails?.crc32

                val details = GameDetails(
                    gameId = game.id,
                    synopsis = effectiveSynopsis ?: existingDetails?.synopsis,
                    publisher = effectivePublisher ?: existingDetails?.publisher,
                    releaseDate = effectiveReleaseDate ?: existingDetails?.releaseDate,
                    genres = effectiveGenres ?: existingDetails?.genres,
                    players = screenScraperData?.players ?: existingDetails?.players,
                    rating = effectiveRating ?: existingDetails?.rating,
                    ratingCount = alphaLemuroidData?.ratingCount ?: existingDetails?.ratingCount,
                    bannerUrl = finalBannerUri,
                    logoUrl = finalLogoUri,
                    boxArtFrontUrl = finalBoxArtUri ?: existingBoxArt ?: game.coverFrontUrl,
                    boxArtBackUrl = finalBackBoxArtUri ?: existingBackBoxArt,
                    screenshotUrlsJson = screenScraperClient.serializeScreenshots(mergedScreenshotUris),
                    videoUrl = finalVideoUri,
                    crc32 = normalizedCrc,
                    scraperSource = effectiveSource,
                    lastScrapedAt = System.currentTimeMillis(),
                )
                retrogradeDb.gameDetailsDao().insertOrUpdate(details)

                val shouldUpdateTitle = cleanTitle.isNotBlank() && cleanTitle != game.title
                val shouldUpdateCover = finalBoxArtUri != null && finalBoxArtUri != game.coverFrontUrl

                if (shouldUpdateTitle || shouldUpdateCover) {
                    val updatedGame = game.copy(
                        title = if (shouldUpdateTitle) cleanTitle else game.title,
                        coverFrontUrl = if (shouldUpdateCover) finalBoxArtUri else game.coverFrontUrl,
                    )
                    retrogradeDb.gameDao().update(listOf(updatedGame))
                }
            } else {
                
                val existing = retrogradeDb.gameDetailsDao().getGameDetailsDirect(game.id)
                val normalizedCrc = crc?.trim()?.uppercase()?.takeIf { it.isNotBlank() && it != "0" }
                    ?: existing?.crc32
                val fallback = (existing ?: GameDetails(
                    gameId = game.id,
                    synopsis = null,
                    genres = null,
                    publisher = game.developer,
                    releaseDate = null,
                    players = null,
                    rating = null,
                    bannerUrl = null,
                    logoUrl = null,
                    boxArtFrontUrl = game.coverFrontUrl,
                    boxArtBackUrl = null,
                    screenshotUrlsJson = "[]",
                    crc32 = normalizedCrc,
                    scraperSource = "None",
                    lastScrapedAt = System.currentTimeMillis(),
                )).copy(
                    crc32 = normalizedCrc,
                    lastScrapedAt = System.currentTimeMillis(),
                )
                retrogradeDb.gameDetailsDao().insertOrUpdate(fallback)
            }
        } catch (e: CancellationException) {
            android.util.Log.i("GameDetailViewModel", "scrapeGame cancelled for gameId=${game.id}")
            throw e
        } catch (e: Exception) {
            android.util.Log.e("GameDetailViewModel", "scrapeGame error: ${e.message}", e)
        } finally {
            isScrapingFlow.value = false
        }
    }

    class Factory(
        private val gameId: Int,
        private val retrogradeDb: RetrogradeDatabase,
        private val settingsManager: SettingsManager,
        private val appContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameDetailViewModel(
                gameId = gameId,
                retrogradeDb = retrogradeDb,
                settingsManager = settingsManager,
                appContext = appContext,
            ) as T
        }
    }
}
