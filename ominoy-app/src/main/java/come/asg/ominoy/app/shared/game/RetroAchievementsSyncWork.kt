package come.asg.ominoy.app.shared.game

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readWebApiCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsProfileCache
import come.asg.ominoy.app.shared.game.RetroAchievementsSyncProgress.GameProgress
import come.asg.ominoy.app.shared.game.RetroAchievementsSyncProgress.update
import come.asg.ominoy.app.mobile.shared.NotificationsManager
import come.asg.ominoy.app.utils.android.createSyncForegroundInfo
import come.asg.ominoy.lib.injection.AndroidWorkerInjection
import come.asg.ominoy.lib.injection.WorkerKey
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import come.asg.ominoy.lib.library.db.entity.Game
import dagger.Binds
import dagger.android.AndroidInjector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.random.Random
import timber.log.Timber
import java.text.Normalizer
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class RetroAchievementsSyncWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    @Inject lateinit var retrogradeDb: RetrogradeDatabase

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)
        val credentials = readWebApiCredentials(applicationContext)
            ?: run { RetroAchievementsSyncProgress.finish("API is not configured"); return Result.success() }
        val notifications = NotificationsManager(applicationContext)
        setForeground(createSyncForegroundInfo(
            NotificationsManager.RETRO_ACHIEVEMENTS_NOTIFICATION_ID,
            notifications.retroAchievementsSyncNotification(),
        ))

        val requestedIds = inputData.getIntArray(INPUT_GAME_IDS)?.toSet()
        val games = withContext(Dispatchers.IO) {
            retrogradeDb.gameDao().selectAll().let { allGames ->
                if (requestedIds == null) allGames else allGames.filter { it.id in requestedIds }
            }
        }
        RetroAchievementsSyncProgress.prepareWorker(games)
        if (games.isEmpty()) {
            RetroAchievementsSyncProgress.finish("No games in library")
            return Result.success()
        }
        val cachedAccountGames = RetroAchievementsProfileCache.read(applicationContext)?.achievements?.games.orEmpty()
        val completed = AtomicInteger(0)
        val synced = AtomicInteger(0)
        val unavailable = AtomicInteger(0)
        val skipped = AtomicInteger(0)
        val semaphore = Semaphore(MAX_PARALLEL_GAMES)

        coroutineScope {
            games.map { game ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        if (isStopped) return@withPermit
                        var countedComplete = false
                        try {
                        val cached = RetroAchievementsEarnedStore.cacheProgressForLibraryGame(applicationContext, game.id)
                        // A complete JSON cache is not sufficient for offline play: the native
                        // cache also needs the ROM MD5, patch, session and unlock responses.
                        // Always identify the ROM and run syncLocalGame during an explicit
                        // refresh so the button makes the game genuinely offline-ready.
                        if (cached.complete) {
                            update(game.id) {
                                it.copy(
                                    downloaded = cached.total,
                                    total = cached.total,
                                    state = "Preparing offline mode…",
                                    complete = false,
                                    failed = false,
                                )
                            }
                        }
                        update(game.id) { it.copy(state = "Identifying game…", complete = false) }
                        notifications.showRetroAchievementsNotification(
                            notifications.retroAchievementsSyncNotification(
                                completed = completed.get(), total = games.size, current = "Identifying ${game.displayName()}",
                            ),
                        )
                        var result: RetroAchievementsEarnedStore.LocalGameSyncResult? = null
                        var fallbackGameId: Int? = null
                        for (attempt in 0 until MAX_ATTEMPTS) {
                            result = if (fallbackGameId == null) {
                                runCatching {
                                    RetroAchievementsEarnedStore.syncLocalGame(
                                        context = applicationContext,
                                        libraryGameId = game.id,
                                        fileUri = game.fileUri,
                                        systemId = game.systemId,
                                        gameTitle = game.displayName(),
                                        username = credentials.username,
                                        apiKey = credentials.apiKey,
                                        onArtworkProgress = { downloaded, total ->
                                            update(game.id) { it.copy(downloaded = downloaded, total = total, state = "Downloading artwork…") }
                                        },
                                    )
                                }.getOrElse { error ->
                                    Timber.w(error, "RetroAchievements sync failed for ${game.displayName()}")
                                    RetroAchievementsEarnedStore.LocalGameSyncResult(false, error = error.javaClass.simpleName)
                                }
                            } else {
                                val sync = runCatching {
                                    RetroAchievementsEarnedStore.syncFromServer(
                                        context = applicationContext,
                                        libraryGameId = game.id,
                                        retroAchievementsGameId = fallbackGameId!!,
                                        username = credentials.username,
                                        apiKey = credentials.apiKey,
                                        gameTitle = game.displayName(),
                                        mirrorExport = false,
                                        downloadArtwork = true,
                                        onArtworkProgress = { downloaded, total ->
                                            update(game.id) { it.copy(downloaded = downloaded, total = total, state = "Downloading artwork…") }
                                        },
                                    )
                                }.getOrElse { error ->
                                    Timber.w(error, "RetroAchievements title fallback failed for ${game.displayName()}")
                                    null
                                }
                                if (sync?.success == true && sync.nativePreparationBody.isNotBlank()) {
                                    RetroAchievementsEarnedStore.prepareOfflineGameForLibraryGame(
                                        context = applicationContext,
                                        fileUri = game.fileUri,
                                        systemId = game.systemId,
                                        retroAchievementsGameId = fallbackGameId!!,
                                        webApiBody = sync.nativePreparationBody,
                                        username = credentials.username,
                                    )
                                }
                                RetroAchievementsEarnedStore.LocalGameSyncResult(true, sync, sync?.error.orEmpty())
                            }
                            if (result?.error in setOf("not_found", "no_hash_match") && fallbackGameId == null) {
                                val matchedAccountGame = cachedAccountGames.firstOrNull { accountGame ->
                                    accountGame.gameId > 0 &&
                                        (titlesMatch(game.displayName(), accountGame.title) || titlesMatch(game.title, accountGame.title)) &&
                                        consoleMatches(game.systemId, accountGame.consoleName)
                                }
                                fallbackGameId = matchedAccountGame?.gameId
                                if (fallbackGameId != null) continue
                            }

                            val nowCached = RetroAchievementsEarnedStore.cacheProgressForLibraryGame(applicationContext, game.id)
                            val errorText = listOfNotNull(result?.error, result?.sync?.error).joinToString(" ").lowercase()
                            val retryable = result?.error == "rate_limited" ||
                                errorText.contains("rate limit") || errorText.contains("rate_limited") ||
                                Regex("http[_ ]5\\d\\d").containsMatchIn(errorText) ||
                                errorText.contains("timeout") || errorText.contains("interruptedioexception") ||
                                errorText.contains("connectexception") || errorText.contains("unknownhostexception") ||
                                errorText.contains("ioexception") ||
                                result?.sync?.deferredByRateLimit == true ||
                                (result?.found == true && (result?.sync?.success != true || !nowCached.complete))
                            if (!retryable || attempt == MAX_ATTEMPTS - 1) break
                            update(game.id) { it.copy(state = "Server busy — retrying shortly") }
                            delay((1_000L shl attempt) + Random.nextLong(200L, 900L))
                        }
                        val refreshed = RetroAchievementsEarnedStore.cacheProgressForLibraryGame(applicationContext, game.id)
                        val noAchievements = refreshed.noAchievements || result?.sync?.responseFormat == "no-achievements"
                        val noSetFound = result?.error in setOf("not_found", "no_hash_match", "unsupported_system")
                        val success = result?.sync?.success == true && (refreshed.complete || noAchievements)
                        if (noSetFound) {
                            skipped.incrementAndGet()
                            update(game.id) {
                                it.copy(downloaded = 0, total = 0, state = "No RetroAchievements set — skipped", complete = true, failed = false)
                            }
                        } else if (success) {
                            synced.incrementAndGet()
                            update(game.id) {
                                it.copy(
                                    downloaded = refreshed.total,
                                    total = refreshed.total,
                                    state = if (noAchievements) "No achievements available" else "✓ Downloaded",
                                    complete = true,
                                    failed = false,
                                )
                            }
                        } else {
                            unavailable.incrementAndGet()
                            val failure = failureState(result, refreshed)
                            Timber.w("RetroAchievements sync failed for ${game.displayName()}: $failure")
                            update(game.id) {
                                it.copy(
                                    downloaded = refreshed.downloaded,
                                    total = refreshed.total.coerceAtLeast(it.total),
                                    state = failure,
                                    complete = true,
                                    failed = true,
                                )
                            }
                        }
                        val done = completed.incrementAndGet()
                        countedComplete = true
                        notifications.showRetroAchievementsNotification(
                            notifications.retroAchievementsSyncNotification(completed = done, total = games.size, current = game.displayName()),
                        )
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            Timber.e(error, "Unexpected per-game RetroAchievements failure for ${game.displayName()}")
                            unavailable.incrementAndGet()
                            if (!countedComplete) completed.incrementAndGet()
                            update(game.id) {
                                it.copy(state = "Sync error: ${error.javaClass.simpleName}", complete = true, failed = true)
                            }
                        }
                    }
                }
            }.awaitAll()
        }
        notifications.showRetroAchievementsNotification(notifications.retroAchievementsCompleteNotification(synced.get(), unavailable.get()))
        RetroAchievementsEarnedStore.exportToSelectedDataFolder(applicationContext)
        RetroAchievementsSyncProgress.finish(
            when {
                unavailable.get() > 0 -> "Sync finished; some games failed"
                skipped.get() > 0 -> "Sync finished; games without RetroAchievements were skipped"
                else -> "All achievements downloaded"
            },
        )
        return Result.success()
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds @dagger.multibindings.IntoMap @WorkerKey(RetroAchievementsSyncWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<RetroAchievementsSyncWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<RetroAchievementsSyncWork>()
    }

    companion object {
        const val UNIQUE_WORK_ID = "RetroAchievementsSyncWork"
        private const val MAX_ATTEMPTS = 3
        private const val MAX_PARALLEL_GAMES = 2
        private const val INPUT_GAME_IDS = "library_game_ids"

        private fun failureState(
            result: RetroAchievementsEarnedStore.LocalGameSyncResult?,
            cached: RetroAchievementsEarnedStore.LibraryCacheProgress,
        ): String {
            val sync = result?.sync
            val error = sync?.error?.takeIf { it.isNotBlank() } ?: result?.error.orEmpty()
            val normalized = error.lowercase()
            return when {
                result?.error in setOf("not_found", "no_hash_match") -> "Not recognized (ROM hash not found)"
                result?.error == "rate_limited" || sync?.httpStatus == 429 || sync?.deferredByRateLimit == true -> "Rate limited — retry"
                normalized.contains("timeout") || normalized.contains("ioexception") || normalized.contains("unknownhost") || normalized.contains("connectexception") -> "Network/timeout — retry"
                sync?.httpStatus?.let { it in 500..599 } == true || Regex("http[_ ]5\\d\\d").containsMatchIn(normalized) -> "Server error (${sync?.httpStatus ?: error}) — retry"
                sync?.httpStatus?.let { it in 400..499 } == true || normalized.startsWith("http_") || normalized.startsWith("http ") -> "Request rejected (${sync?.httpStatus ?: error})"
                sync?.responseFormat?.startsWith("invalid") == true || sync?.responseFormat?.startsWith("missing") == true || normalized.contains("jsonexception") -> "Invalid server response"
                result?.found == true && sync?.success == true -> "Artwork incomplete (${cached.downloaded}/${cached.total})"
                error.isNotBlank() -> "Sync error: $error"
                else -> "Sync failed — retry"
            }
        }

        private fun consoleMatches(systemId: String, consoleName: String): Boolean {
            val console = consoleName.lowercase()
            return when (systemId.lowercase()) {
                "gb" -> console.contains("game boy") && !console.contains("color") && !console.contains("advance")
                "gbc" -> console.contains("game boy color")
                "gba" -> console.contains("game boy advance")
                else -> false
            }
        }

        private fun titlesMatch(localTitle: String, accountTitle: String): Boolean {
            fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replace(Regex("\\p{Mn}+"), "")
                .lowercase()
                .replace(Regex("\\([^)]*\\)|\\[[^]]*]"), " ")
                .replace(Regex("[^a-z0-9]+"), " ")
                .trim()
                .replace(Regex("\\s+"), " ")
            val normalizedLocal = normalize(localTitle)
            val normalizedAccount = normalize(accountTitle)
            if (normalizedLocal.isEmpty() || normalizedAccount.isEmpty()) return false
            if (normalizedLocal == normalizedAccount) return true
            fun tokens(value: String): Set<String> = value.split(' ')
                .filter { it.length > 2 && it !in setOf("usa", "europe", "japan", "world", "version", "rev", "revision", "the", "and") }
                .toSet()
            val local = tokens(normalizedLocal)
            val remote = tokens(normalizedAccount)
            if (local.isEmpty() || remote.isEmpty()) return false
            val common = local.intersect(remote).size
            return common >= 2 && common.toFloat() / minOf(local.size, remote.size) >= .6f
        }

        fun enqueue(context: Context, gameIds: Set<Int>? = null) {
            val request = OneTimeWorkRequestBuilder<RetroAchievementsSyncWork>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(
                    if (gameIds == null) Data.EMPTY
                    else Data.Builder().putIntArray(INPUT_GAME_IDS, gameIds.toIntArray()).build(),
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID, ExistingWorkPolicy.KEEP, request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_ID)
        }
    }
}
