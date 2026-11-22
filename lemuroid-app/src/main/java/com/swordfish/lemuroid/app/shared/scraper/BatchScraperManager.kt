package com.swordfish.lemuroid.app.shared.scraper

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.common.kotlin.calculateRomCrc32
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameDetails
import com.swordfish.lemuroid.lib.scraper.AlphaLemuroidClient
import com.swordfish.lemuroid.lib.scraper.MediaStorageManager
import com.swordfish.lemuroid.lib.scraper.ScreenScraperClient
import com.swordfish.lemuroid.lib.scraper.ScreenScraperSystemMapper
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class BatchScrapeProgress(
    val inProgress: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val currentGameTitle: String = "",
    val metaSystemName: String = "",
)

private data class DownloadedMediaBundle(
    val boxArtUri: String?,
    val backBoxArtUri: String?,
    val bannerUri: String?,
    val logoUri: String?,
    val screenshotUris: List<String>,
    val videoUri: String?,
)

class BatchScraperManager(
    private val appContext: Context,
    private val retrogradeDb: RetrogradeDatabase,
    private val settingsManager: SettingsManager,
) {
    private val screenScraperClient = ScreenScraperClient()
    private val alphaLemuroidClient = AlphaLemuroidClient()
    private val mediaStorageManager = MediaStorageManager(appContext)
    private val libretroDbManager by lazy { LibretroDBManager(appContext) }
    private val notificationsManager = NotificationsManager(appContext)
    private val notificationManagerCompat = NotificationManagerCompat.from(appContext)

    private val _scrapeProgress = MutableStateFlow(BatchScrapeProgress())
    val scrapeProgress = _scrapeProgress.asStateFlow()

    private var currentJob: Job? = null

    fun cancel() {
        currentJob?.cancel()
        currentJob = null
        _scrapeProgress.value = BatchScrapeProgress(inProgress = false)
        dismissNotification()
    }

    private fun postNotification(
        notification: Notification,
        notificationId: Int = NotificationsManager.SCRAPER_NOTIFICATION_ID,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        runCatching {
            notificationManagerCompat.notify(notificationId, notification)
        }
    }

    private fun dismissNotification() {
        runCatching {
            notificationManagerCompat.cancel(NotificationsManager.SCRAPER_NOTIFICATION_ID)
        }
    }

    suspend fun scrapePlatform(
        metaSystemID: MetaSystemID,
        overwrite: Boolean = false,
    ): Int = withContext(Dispatchers.IO) {
        if (_scrapeProgress.value.inProgress) return@withContext 0

        currentJob = coroutineContext[Job]
        activeInstance = this@BatchScraperManager
        val systemTitle = appContext.getString(metaSystemID.titleResId)
        try {
            val systemDbNames = metaSystemID.systemIDs.map { it.dbname }
            val allGames = retrogradeDb.gameDao().selectAllBySystems(systemDbNames)
            val mediaSources = settingsManager.screenScraperMediaSources()
            val gamesToScrape = if (overwrite) {
                allGames
            } else {
                allGames.filter { game ->
                    val details = retrogradeDb.gameDetailsDao().getGameDetailsDirect(game.id)
                    needsScrapeForEnabledMedia(game, details, mediaSources)
                }
            }

            if (gamesToScrape.isEmpty()) {
                return@withContext 0
            }

            _scrapeProgress.value = BatchScrapeProgress(
                inProgress = true,
                current = 0,
                total = gamesToScrape.size,
                metaSystemName = systemTitle,
            )

            val processedCount = AtomicInteger(0)
            val scrapedCount = AtomicInteger(0)
            val gameSemaphore = Semaphore(MAX_CONCURRENT_GAMES)

            coroutineScope {
                gamesToScrape.map { game ->
                    async {
                        gameSemaphore.withPermit {
                            ensureActive()
                            val gameTitle = game.displayName()
                            _scrapeProgress.value = _scrapeProgress.value.copy(
                                current = processedCount.get(),
                                currentGameTitle = gameTitle,
                            )

                            val success = scrapeSingleGame(game)
                            val done = processedCount.incrementAndGet()
                            val scraped = if (success) scrapedCount.incrementAndGet() else scrapedCount.get()

                            ensureActive()
                            _scrapeProgress.value = _scrapeProgress.value.copy(
                                current = done,
                                currentGameTitle = gameTitle,
                            )
                            postNotification(
                                notificationsManager.scrapingProgressNotification(
                                    platformName = systemTitle,
                                    processed = done,
                                    scraped = scraped,
                                    total = gamesToScrape.size,
                                    currentGameTitle = gameTitle,
                                ),
                            )
                        }
                    }
                }.awaitAll()
            }

            val finalScrapedCount = scrapedCount.get()
            postNotification(
                notification = notificationsManager.scrapingCompleteNotification(
                    platformName = systemTitle,
                    scrapedCount = finalScrapedCount,
                    totalCount = gamesToScrape.size,
                ),
                notificationId = NotificationsManager.SCRAPER_COMPLETE_NOTIFICATION_ID,
            )
            finalScrapedCount
        } catch (e: CancellationException) {
            android.util.Log.i("BatchScraperManager", "scrapePlatform cancelled")
            dismissNotification()
            throw e
        } finally {
            _scrapeProgress.value = BatchScrapeProgress(inProgress = false)
            currentJob = null
            if (activeInstance === this@BatchScraperManager) {
                activeInstance = null
            }
        }
    }

    suspend fun scrapeAllPlatforms(
        overwrite: Boolean = false,
    ): Int = withContext(Dispatchers.IO) {
        if (_scrapeProgress.value.inProgress) return@withContext 0

        currentJob = coroutineContext[Job]
        activeInstance = this@BatchScraperManager
        try {
            val mediaSources = settingsManager.screenScraperMediaSources()
            val allPlatforms = MetaSystemID.values()
            val platformsToScrape = mutableListOf<Pair<MetaSystemID, List<Game>>>()

            for (meta in allPlatforms) {
                val systemDbNames = meta.systemIDs.map { it.dbname }
                val games = retrogradeDb.gameDao().selectAllBySystems(systemDbNames)
                val gamesToScrape = if (overwrite) {
                    games
                } else {
                    games.filter { game ->
                        val details = retrogradeDb.gameDetailsDao().getGameDetailsDirect(game.id)
                        needsScrapeForEnabledMedia(game, details, mediaSources)
                    }
                }
                if (gamesToScrape.isNotEmpty()) {
                    platformsToScrape.add(meta to gamesToScrape)
                }
            }

            val overallTotal = platformsToScrape.sumOf { it.second.size }
            if (overallTotal == 0) {
                return@withContext 0
            }

            val allPlatformsTitle = appContext.getString(com.swordfish.lemuroid.R.string.scraper_notification_all_platforms)
            _scrapeProgress.value = BatchScrapeProgress(
                inProgress = true,
                current = 0,
                total = overallTotal,
                metaSystemName = allPlatformsTitle,
            )

            val overallProcessedCount = AtomicInteger(0)
            val overallScrapedCount = AtomicInteger(0)
            val gameSemaphore = Semaphore(MAX_CONCURRENT_GAMES)

            for ((meta, systemGames) in platformsToScrape) {
                ensureActive()
                val systemTitle = appContext.getString(meta.titleResId)
                val consoleTag = when (meta) {
                    MetaSystemID.GENESIS -> "[MD]"
                    MetaSystemID.ATARI2600 -> "[A2600]"
                    MetaSystemID.ATARI7800 -> "[A7800]"
                    MetaSystemID.PC_ENGINE -> "[PCE]"
                    else -> "[${meta.name}]"
                }

                val systemProcessed = AtomicInteger(0)
                val systemScraped = AtomicInteger(0)
                val systemTotal = systemGames.size

                coroutineScope {
                    systemGames.map { game ->
                        async {
                            gameSemaphore.withPermit {
                                ensureActive()
                                val gameTitle = "$consoleTag ${game.displayName()}"
                                _scrapeProgress.value = _scrapeProgress.value.copy(
                                    current = overallProcessedCount.get(),
                                    currentGameTitle = gameTitle,
                                    metaSystemName = allPlatformsTitle,
                                )

                                val success = scrapeSingleGame(game)
                                val sDone = systemProcessed.incrementAndGet()
                                val sScraped = if (success) systemScraped.incrementAndGet() else systemScraped.get()
                                val oDone = overallProcessedCount.incrementAndGet()
                                val oScraped = if (success) overallScrapedCount.incrementAndGet() else overallScrapedCount.get()

                                ensureActive()
                                _scrapeProgress.value = _scrapeProgress.value.copy(
                                    current = oDone,
                                    currentGameTitle = gameTitle,
                                    metaSystemName = allPlatformsTitle,
                                )
                                postNotification(
                                    notificationsManager.scrapingProgressNotification(
                                        platformName = systemTitle,
                                        processed = sDone,
                                        scraped = sScraped,
                                        total = systemTotal,
                                        currentGameTitle = gameTitle,
                                        overallProcessed = oDone,
                                        overallTotal = overallTotal,
                                    ),
                                )
                            }
                        }
                    }.awaitAll()
                }
            }

            val finalScrapedCount = overallScrapedCount.get()
            postNotification(
                notification = notificationsManager.scrapingCompleteNotification(
                    platformName = allPlatformsTitle,
                    scrapedCount = finalScrapedCount,
                    totalCount = overallTotal,
                    isAllPlatforms = true,
                ),
                notificationId = NotificationsManager.SCRAPER_COMPLETE_NOTIFICATION_ID,
            )
            finalScrapedCount
        } catch (e: CancellationException) {
            android.util.Log.i("BatchScraperManager", "scrapeAllPlatforms cancelled")
            dismissNotification()
            throw e
        } finally {
            _scrapeProgress.value = BatchScrapeProgress(inProgress = false)
            currentJob = null
            if (activeInstance === this@BatchScraperManager) {
                activeInstance = null
            }
        }
    }

    companion object {
        private const val MAX_CONCURRENT_GAMES = 5
        private const val MAX_CONCURRENT_DOWNLOADS = 5

        @Volatile
        private var activeInstance: BatchScraperManager? = null

        fun cancelActive() {
            activeInstance?.cancel()
        }
    }

    private fun needsScrapeForEnabledMedia(
        game: Game,
        details: GameDetails?,
        mediaSources: Set<String>,
    ): Boolean {
        if (details == null || details.lastScrapedAt == 0L) return true
        if (!mediaStorageManager.isMediaUriValid(details.boxArtFrontUrl)) return true

        val romBasename = game.fileName.substringBeforeLast('.')
        if (mediaSources.contains("banner") && !mediaStorageManager.isMediaUriValid(details.bannerUrl)) {
            return true
        }
        if (mediaSources.contains("video") && !mediaStorageManager.isMediaUriValid(details.videoUrl)) {
            return true
        }
        if (mediaSources.contains("fanart") &&
            !mediaStorageManager.hasLocalMedia(game.systemId, MediaStorageManager.MediaType.FANART, romBasename)
        ) {
            return true
        }

        if (mediaSources.contains("ss") || mediaSources.contains("sstitle")) {
            val validShots = details.screenshotUrlsJson
                ?.let { screenScraperClient.deserializeScreenshots(it) }
                ?.filter { mediaStorageManager.isMediaUriValid(it) }
                ?: emptyList()
            val hasTitle = validShots.any {
                it.contains("/titles/", ignoreCase = true) ||
                    it.contains("title_", ignoreCase = true) ||
                    it.contains("sstitle", ignoreCase = true)
            }
            val hasGameplayShot = validShots.any {
                !it.contains("/titles/", ignoreCase = true) &&
                    !it.contains("title_", ignoreCase = true) &&
                    !it.contains("sstitle", ignoreCase = true)
            }
            if (mediaSources.contains("ss") && !hasGameplayShot) return true
            if (mediaSources.contains("sstitle") && !hasTitle) return true
        }

        return false
    }

    suspend fun scrapeSingleGame(game: Game): Boolean = withContext(Dispatchers.IO) {
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

            val isZip = game.fileUri.endsWith(".zip", ignoreCase = true) ||
                game.fileName.endsWith(".zip", ignoreCase = true)

            if (uri != null) {
                runCatching {
                    appContext.contentResolver.openInputStream(uri)?.use { stream ->
                        crc = stream.calculateRomCrc32(
                            fileName = game.fileName,
                            isArcadeZip = isArcadeZip,
                            isZipArchive = isZip,
                            targetEntryName = game.fileName,
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

            val mediaSemaphore = Semaphore(MAX_CONCURRENT_DOWNLOADS)
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

            val (
                finalBoxArtUri,
                finalBackBoxArtUri,
                finalBannerUri,
                finalLogoUri,
                localScreenshotUris,
                finalVideoUri,
            ) = coroutineScope {
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

                val screenshotsDeferred = distinctScreenshots.mapIndexed { idx, shotUrl ->
                    async {
                        val isTitle = shotUrl.contains("sstitle", ignoreCase = true) || shotUrl.contains("title", ignoreCase = true)
                        val cat = if (isTitle) MediaStorageManager.MediaType.TITLES else MediaStorageManager.MediaType.SCREENSHOTS
                        val localShot = mediaSemaphore.withPermit {
                            mediaStorageManager.saveOrDownloadMedia(shotUrl, systemId, cat, romBasename, downloadedUrlMap, index = idx + 1)
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
                DownloadedMediaBundle(
                    boxArtUri = boxArtDeferred.await(),
                    backBoxArtUri = null,
                    bannerUri = bannerDeferred.await(),
                    logoUri = logoDeferred.await(),
                    screenshotUris = screenshotsDeferred.awaitAll(),
                    videoUri = videoDeferred.await(),
                )
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
                true
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
                false
            }
        } catch (e: Throwable) {
            android.util.Log.e("BatchScraperManager", "Error scraping game ${game.id}: ${e.message}", e)
            false
        }
    }
}
