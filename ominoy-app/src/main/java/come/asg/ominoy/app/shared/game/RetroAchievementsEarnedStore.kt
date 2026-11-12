package come.asg.ominoy.app.shared.game

import android.content.Context
import android.net.Uri
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsWebApiCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readGameplayCredentials
import come.asg.ominoy.lib.storage.DirectoriesManager
import com.swordfish.libretrodroid.RetroAchievementsNativeBridge
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** RA-ID keyed achievement cache. Web API credentials are never used by native gameplay. */
object RetroAchievementsEarnedStore {
    private const val BASE_URL = "https://retroachievements.org"
    private const val MEDIA_URL = "https://media.retroachievements.org"
    private const val METADATA_FILE = "achievements.json"
    private const val EARNED_FILE = "earned-achievements.json"
    private const val USERS_DIR = "users"
    private const val EARNED_ISOLATION_PREFS = "retro_achievements_earned_isolation"
    private const val EARNED_ISOLATION_DONE = "legacy_earned_cleared"
    private const val GAME_FILE = "game.json"
    private const val DIAGNOSTICS_FILE = "sync-diagnostics.json"
    private const val LEGACY_PREFS = "retro_achievements_earned"
    private const val LEGACY_KEY_PREFIX = "game_"
    private const val MIGRATION_PREFS = "retro_achievements_storage"
    private const val MIGRATION_DONE = "external_files_migration_done"
    private val writeLock = Any()
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    data class Achievement(
        val id: Long,
        val title: String,
        val description: String,
        val points: Int,
        val badgeUrl: String,
        val badgePath: String,
        val earned: Boolean,
        val earnedAt: Long,
        val raGameId: Int = 0,
        val lockedBadgePath: String = "",
        val badgeName: String = "",
        val hardcoreEarnedAt: Long = 0L,
        val localLibraryGameId: Int = 0,
        val romHash: String = "",
        val gameTitle: String = "",
    )

    data class SyncResult(
        val success: Boolean,
        val httpStatus: Int = 0,
        val gameId: Int,
        val achievementsReturned: Int = 0,
        val earnedReturned: Int = 0,
        val badgesDownloaded: Int = 0,
        val badgesFailed: Int = 0,
        val iconDownloaded: Boolean = false,
        val gameTitle: String = "",
        val responseFormat: String = "",
        val error: String = "",
        val deferredByRateLimit: Boolean = false,
        val nativePreparationBody: String = "",
    )

    data class LocalGameSyncResult(
        val found: Boolean,
        val sync: SyncResult? = null,
        val error: String = "",
    )

    private data class GameHashLookup(val gameId: Int = 0, val gameTitle: String = "", val error: String = "")

    /** Canonical cache. This must never resolve to a user-selected/shared-storage path. */
    private fun root(context: Context): File {
        val app = context.applicationContext
        val destination = File(app.filesDir, "achievements")
        migrateAchievementStorage(app, destination)
        return destination
    }

    /** Migrates the old app-specific external achievement tree without deleting on partial failure. */
    private fun migrateAchievementStorage(context: Context, destination: File) {
        val prefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MIGRATION_DONE, false)) return
        val sources = linkedSetOf<File>().apply {
            context.getExternalFilesDir(null)?.let { add(File(it, "achievements")) }
            DirectoriesManager(context).getSelectedDataDirectory()?.let { add(File(it, "achievements")) }
            add(File(context.filesDir, "achievements-legacy"))
        }.filter { it.isDirectory && it.canonicalFile != destination.canonicalFile }
        if (sources.isEmpty()) {
            prefs.edit().putBoolean(MIGRATION_DONE, true).apply()
            return
        }
        runCatching {
            destination.mkdirs()
            sources.forEach { source ->
                source.listFiles()?.forEach { child ->
                    val target = File(destination, child.name)
                    if (child.name == "${EARNED_FILE}") copyAndVerify(child, target)
                    else if (!target.exists()) copyAndVerify(child, target)
                }
                verifyTree(source, destination)
                source.deleteRecursively()
            }
            prefs.edit().putBoolean(MIGRATION_DONE, true).apply()
        }.onFailure { error ->
            Timber.e(error, "Achievement storage migration incomplete; source data was retained")
        }
    }

    private fun copyAndVerify(source: File, target: File) {
        if (source.isDirectory) {
            source.listFiles()?.forEach { copyAndVerify(it, File(target, it.name)) }
            return
        }
        target.parentFile?.mkdirs()
        source.copyTo(target, overwrite = true)
        check(target.isFile && target.length() == source.length()) { "Failed to verify ${source.name}" }
    }

    private fun verifyTree(source: File, destination: File) {
        source.walkTopDown().filter { it.isFile }.forEach { file ->
            val relative = file.relativeTo(source)
            val copy = File(destination, relative.path)
            check(copy.isFile && copy.length() == file.length()) { "Missing migrated file ${relative.path}" }
        }
    }

    /** Moves the old selected-folder/internal cache once. Badges move with their game directory. */
    suspend fun migrateLegacyStorage(context: Context) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MIGRATION_DONE, false)) return@withContext
        runCatching {
            val destination = root(app)
            val candidates = linkedSetOf<File>()
            candidates += File(app.filesDir, "achievements")
            DirectoriesManager(app).getSelectedDataDirectory()?.let { candidates += File(it, "achievements") }
            candidates.filter { it.isDirectory && it.canonicalFile != destination.canonicalFile }.forEach { legacyRoot ->
                legacyRoot.listFiles()?.forEach { legacyGame ->
                    val id = gameIdInDirectory(legacyGame)
                    if (id <= 0) return@forEach
                    val title = readJson(File(legacyGame, GAME_FILE))?.optString("title").orEmpty()
                        .ifBlank { readFile(File(legacyGame, METADATA_FILE)).firstOrNull()?.gameTitle.orEmpty() }
                        .ifBlank { "Game $id" }
                    val target = gameDir(app, id, title)
                    legacyGame.copyRecursively(target, overwrite = false)
                    legacyGame.deleteRecursively()
                }
            }
            prefs.edit().putBoolean(MIGRATION_DONE, true).apply()
        }.onFailure { Timber.w(it, "Could not migrate RetroAchievements cache") }
    }

    private fun findGameDir(context: Context, raGameId: Int): File? =
        root(context).listFiles()?.firstOrNull { dir ->
            dir.isDirectory && gameIdInDirectory(dir) == raGameId
        }

    private fun gameIdInDirectory(dir: File): Int {
        val gameId = readJson(File(dir, GAME_FILE))?.optInt("raGameId", 0) ?: 0
        if (gameId > 0) return gameId
        return readFile(File(dir, METADATA_FILE)).firstOrNull { it.raGameId > 0 }?.raGameId
            ?: dir.name.toIntOrNull()
            ?: 0
    }

    private fun gameDir(context: Context, raGameId: Int): File =
        findGameDir(context, raGameId) ?: File(root(context), raGameId.toString()).apply { mkdirs() }

    private fun gameDir(context: Context, raGameId: Int, title: String): File {
        if (raGameId <= 0 || title.isBlank()) return gameDir(context, raGameId)
        val current = findGameDir(context, raGameId)
        if (current != null) return current
        val target = File(root(context).apply { mkdirs() }, raGameId.toString())
        target.mkdirs()
        atomicWrite(File(target, GAME_FILE), JSONObject().put("raGameId", raGameId).put("title", title.trim()).toString())
        return target
    }

    private fun updateStoredPaths(dir: File, oldPath: String, newPath: String) {
        listOf(METADATA_FILE, EARNED_FILE).forEach { name ->
            val file = File(dir, name)
            val array = runCatching { JSONArray(file.readText()) }.getOrNull() ?: return@forEach
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.apply {
                    put("badgePath", optString("badgePath").replace(oldPath, newPath))
                    put("lockedBadgePath", optString("lockedBadgePath").replace(oldPath, newPath))
                }
            }
            atomicWrite(file, array.toString())
        }
    }

    private fun metadataFile(context: Context, raGameId: Int) = File(gameDir(context, raGameId), METADATA_FILE)

    private fun accountKey(context: Context): String = readGameplayCredentials(context)?.username
        ?.trim()?.lowercase()?.replace(Regex("[^a-z0-9._-]"), "_")?.takeIf { it.isNotBlank() }
        ?: "anonymous"

    private fun accountEarnedFile(context: Context, raGameId: Int): File =
        File(File(gameDir(context, raGameId), USERS_DIR).apply { mkdirs() }, "${accountKey(context)}/$EARNED_FILE")

    private fun clearLegacyEarnedFlagsOnce(context: Context) {
        val prefs = context.getSharedPreferences(EARNED_ISOLATION_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(EARNED_ISOLATION_DONE, false)) return
        root(context).listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val file = File(dir, METADATA_FILE)
            val values = readFile(file)
            if (values.any { it.earned || it.earnedAt > 0L || it.hardcoreEarnedAt > 0L }) {
                atomicWrite(file, JSONArray().apply {
                    values.forEach { put(toJson(it.copy(earned = false, earnedAt = 0L, hardcoreEarnedAt = 0L))) }
                }.toString())
            }
        }
        prefs.edit().putBoolean(EARNED_ISOLATION_DONE, true).apply()
    }

    fun read(context: Context, libraryGameId: Int): List<Achievement> {
        if (libraryGameId <= 0) return emptyList()
        clearLegacyEarnedFlagsOnce(context)
        val current = root(context).listFiles()?.asSequence().orEmpty()
            .filter { it.isDirectory }
            .mapNotNull { dir -> readForGame(context, gameIdInDirectory(dir)).takeIf { values -> values.any { it.localLibraryGameId == libraryGameId } } }
            .flatten().toList()
        if (current.isNotEmpty()) return current.sortedByDescending { it.earnedAt }
        val legacy = context.applicationContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
            .getString("$LEGACY_KEY_PREFIX$libraryGameId", null)
        return parseArray(legacy ?: "[]", libraryGameId = libraryGameId)
    }

    fun readAll(context: Context): List<Achievement> =
        run {
        clearLegacyEarnedFlagsOnce(context)
        root(context).listFiles()?.asSequence().orEmpty()
            .filter { it.isDirectory }
            .flatMap { dir ->
                val title = readJson(File(dir, GAME_FILE))?.optString("title").orEmpty().ifBlank { dir.name }
                readForGame(context, gameIdInDirectory(dir)).asSequence().map { achievement ->
                    if (achievement.gameTitle.isBlank()) achievement.copy(gameTitle = title) else achievement
                }
            }
            .sortedWith(compareBy<Achievement> { it.gameTitle.lowercase() }.thenBy { it.title.lowercase() })
            .toList()
        }

    fun readForGame(context: Context, raGameId: Int): List<Achievement> =
        if (raGameId > 0) {
            val metadata = readFile(metadataFile(context, raGameId))
            val earned = readFile(accountEarnedFile(context, raGameId)).associateBy { it.id }
            metadata.map { base ->
                earned[base.id]?.let { user -> base.copy(earned = user.earned, earnedAt = user.earnedAt, hardcoreEarnedAt = user.hardcoreEarnedAt) } ?: base.copy(earned = false, earnedAt = 0L, hardcoreEarnedAt = 0L)
            }
        } else emptyList()

    data class LibraryCacheProgress(
        val downloaded: Int,
        val total: Int,
        val logoDownloaded: Boolean = false,
        val noAchievements: Boolean = false,
    ) {
        val complete: Boolean get() = noAchievements || (total > 0 && downloaded == total && logoDownloaded)
    }

    /** Counts cached achievement entries with both badge variants present for a library game. */
    fun cacheProgressForLibraryGame(context: Context, libraryGameId: Int): LibraryCacheProgress {
        val items = read(context, libraryGameId)
        val noAchievements = root(context).listFiles()?.asSequence().orEmpty()
            .filter { it.isDirectory }
            .mapNotNull { dir -> readJson(File(dir, DIAGNOSTICS_FILE)) }
            .any { it.optInt("localLibraryGameId") == libraryGameId && it.optString("responseFormat") == "no-achievements" }
        if (items.isEmpty()) return LibraryCacheProgress(0, 0, noAchievements = noAchievements)
        val downloaded = items.count { item ->
            item.badgePath.isNotBlank() && File(item.badgePath).isFile &&
                item.lockedBadgePath.isNotBlank() && File(item.lockedBadgePath).isFile
        }
        val gameDirectory = items.firstNotNullOfOrNull { item ->
            item.badgePath.takeIf { it.isNotBlank() }?.let { File(File(it).parentFile?.parentFile, "logo.png") }
        }
        return LibraryCacheProgress(downloaded, items.size, gameDirectory?.isFile == true, noAchievements)
    }

    fun find(context: Context, libraryGameId: Int, achievementId: Long): Achievement? =
        read(context, libraryGameId).firstOrNull { it.id == achievementId }

    fun findByRaGame(context: Context, raGameId: Int, achievementId: Long): Achievement? =
        readForGame(context, raGameId).firstOrNull { it.id == achievementId }

    fun record(context: Context, libraryGameId: Int, achievement: Achievement) {
        val gameId = achievement.raGameId
        if (gameId <= 0) return
        gameDir(context, gameId, achievement.gameTitle)
        val existing = readForGame(context, gameId).associateBy { it.id }.toMutableMap()
        val old = existing[achievement.id]
        existing[achievement.id] = achievement.copy(
            localLibraryGameId = libraryGameId,
            badgeUrl = achievement.badgeUrl.ifBlank { old?.badgeUrl.orEmpty() },
            badgePath = achievement.badgePath.ifBlank { old?.badgePath.orEmpty() },
            lockedBadgePath = achievement.lockedBadgePath.ifBlank { old?.lockedBadgePath.orEmpty() },
            earned = true,
            earnedAt = old?.earnedAt?.takeIf { it > 0 } ?: achievement.earnedAt,
            gameTitle = achievement.gameTitle.ifBlank { old?.gameTitle.orEmpty() },
        )
        write(context, gameId, existing.values)
    }

    suspend fun syncFromServer(
        context: Context,
        libraryGameId: Int,
        retroAchievementsGameId: Int,
        username: String,
        apiKey: String,
        romHash: String = "",
        gameTitle: String = "",
        mirrorExport: Boolean = true,
        downloadArtwork: Boolean = true,
        onArtworkProgress: ((completed: Int, total: Int) -> Unit)? = null,
    ): SyncResult = withContext(Dispatchers.IO) {
        migrateLegacyStorage(context)
        if (retroAchievementsGameId <= 0 || username.isBlank() || apiKey.isBlank()) {
            return@withContext SyncResult(false, gameId = retroAchievementsGameId, error = "Web API key is missing")
        }
        val query = "y=${encode(apiKey)}&u=${encode(username)}&g=$retroAchievementsGameId"
        val request = Request.Builder().url("$BASE_URL/API/API_GetGameInfoAndUserProgress.php?$query")
            .header("User-Agent", "OMINOY/1.18.0").get().build()
        val result = runCatching {
            http.newCall(request).execute().use { response ->
                val status = response.code
                if (status == 429) {
                    val r = SyncResult(false, status, retroAchievementsGameId, error = "deferred by server rate limit", deferredByRateLimit = true)
                    writeDiagnostic(context, r, libraryGameId, romHash)
                    return@use r
                }
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val r = SyncResult(false, status, retroAchievementsGameId, error = "HTTP $status")
                    writeDiagnostic(context, r, libraryGameId, romHash)
                    return@use r
                }
                val json = JSONObject(body)
                val rawAchievements = json.opt("Achievements")
                val achievements = when (rawAchievements) {
                    is JSONObject -> rawAchievements
                    is JSONArray -> JSONObject().apply {
                        for (index in 0 until rawAchievements.length()) {
                            val item = rawAchievements.optJSONObject(index) ?: continue
                            val id = item.optLong("ID", index.toLong() + 1L)
                            put(id.toString(), item)
                        }
                    }
                    null, JSONObject.NULL -> JSONObject()
                    else -> null
                }
                if (achievements == null) {
                    val payloadType = rawAchievements?.javaClass?.simpleName ?: "null"
                    val r = SyncResult(false, status, retroAchievementsGameId, error = "invalid Achievements payload ($payloadType)", responseFormat = "invalid-achievements")
                    writeDiagnostic(context, r, libraryGameId, romHash)
                    return@use r
                }
                val resolvedGameTitle = json.optString("Title").trim()
                    .ifBlank { gameTitle.trim() }
                    .ifBlank { "Game $retroAchievementsGameId" }
                gameDir(context, retroAchievementsGameId, resolvedGameTitle)
                if (achievements.length() == 0) {
                    write(context, retroAchievementsGameId, emptyList())
                    val r = SyncResult(
                        success = true,
                        httpStatus = status,
                        gameId = retroAchievementsGameId,
                        gameTitle = resolvedGameTitle,
                        responseFormat = "no-achievements",
                    )
                    writeDiagnostic(context, r, libraryGameId, romHash)
                    return@use r
                }
                val old = readForGame(context, retroAchievementsGameId).associateBy { it.id }
                val items = mutableListOf<Achievement>()
                var earned = 0
                val pendingArtwork = mutableMapOf<Long, Pair<String, String>>()
                achievements.keys().forEach { key ->
                    val item = achievements.optJSONObject(key) ?: return@forEach
                    val id = item.optLong("ID", key.toLongOrNull() ?: 0L)
                    if (id <= 0) return@forEach
                    val oldItem = old[id]
                    val badgeName = item.optString("BadgeName").trim()
                    val badgeUrl = item.optString("BadgeURL").trim().ifBlank { if (badgeName.isBlank()) "" else "$MEDIA_URL/Badge/$badgeName.png" }
                    val lockedUrl = if (badgeName.isBlank()) "" else "$MEDIA_URL/Badge/${badgeName}_lock.png"
                    val earnedDate = parseDate(item.optString("DateEarned"))
                    val hardcoreDate = parseDate(item.optString("DateEarnedHardcore"))
                    val localBadge = oldItem?.badgePath?.takeIf { File(it).isFile }
                        .orEmpty()
                    val localLocked = oldItem?.lockedBadgePath?.takeIf { File(it).isFile }
                        .orEmpty()
                    if (downloadArtwork && (localBadge.isBlank() || localLocked.isBlank())) {
                        pendingArtwork[id] = badgeUrl to lockedUrl
                    }
                    if (earnedDate > 0 || oldItem?.earned == true) earned++
                    items += Achievement(
                        id = id,
                        title = item.optString("Title", oldItem?.title ?: "Achievement"),
                        description = item.optString("Description", oldItem?.description.orEmpty()),
                        points = item.optInt("Points", oldItem?.points ?: 0),
                        badgeUrl = badgeUrl.ifBlank { oldItem?.badgeUrl.orEmpty() },
                        badgePath = localBadge.ifBlank { oldItem?.badgePath.orEmpty() },
                        lockedBadgePath = localLocked.ifBlank { oldItem?.lockedBadgePath.orEmpty() },
                        earned = oldItem?.earned == true || earnedDate > 0,
                        earnedAt = earnedDate.takeIf { it > 0 } ?: oldItem?.earnedAt ?: 0L,
                        raGameId = retroAchievementsGameId,
                        badgeName = badgeName,
                        hardcoreEarnedAt = hardcoreDate,
                        localLibraryGameId = libraryGameId,
                        romHash = romHash,
                        gameTitle = resolvedGameTitle,
                    )
                }
                val iconUrl = mediaUrl(json.optString("ImageIcon"))
                val iconFile = File(File(gameDir(context, retroAchievementsGameId, resolvedGameTitle), "badges"), "icon.png")
                val iconNeedsDownload = downloadArtwork && iconUrl.isNotBlank() && !(iconFile.isFile && iconFile.length() > 0)
                val artworkTotal = pendingArtwork.values.sumOf { (badge, locked) ->
                    (if (badge.isNotBlank()) 1 else 0) + (if (locked.isNotBlank()) 1 else 0)
                } + if (iconNeedsDownload) 1 else 0
                var artworkCompleted = items.count { item ->
                    item.badgePath.isNotBlank() && File(item.badgePath).isFile &&
                        item.lockedBadgePath.isNotBlank() && File(item.lockedBadgePath).isFile
                }
                val progressLock = Any()
                fun reportArtworkProgress() {
                    val completed = synchronized(progressLock) { ++artworkCompleted }
                    onArtworkProgress?.invoke(completed, items.size)
                }
                onArtworkProgress?.invoke(artworkCompleted, items.size)
                val (artwork, iconPath) = if (downloadArtwork && artworkTotal > 0) {
                    coroutineScope {
                        val semaphore = Semaphore(8)
                        val artworkTasks = pendingArtwork.mapValues { (id, urls) ->
                            async {
                                semaphore.withPermit {
                                    val current = items.first { it.id == id }
                                    val badge = if (current.badgePath.isNotBlank()) current.badgePath else
                                        download(context, retroAchievementsGameId, resolvedGameTitle, "${id}.png", urls.first)
                                    val locked = if (current.lockedBadgePath.isNotBlank()) current.lockedBadgePath else
                                        download(context, retroAchievementsGameId, resolvedGameTitle, "${id}_lock.png", urls.second)
                                    if (badge.isNotBlank() && File(badge).isFile && locked.isNotBlank() && File(locked).isFile) {
                                        reportArtworkProgress()
                                    }
                                    badge to locked
                                }
                            }
                        }
                        val iconTask = async {
                            if (!iconNeedsDownload) "" else semaphore.withPermit {
                                download(context, retroAchievementsGameId, resolvedGameTitle, "icon.png", iconUrl)
                            }
                        }
                        val completedArtwork = artworkTasks.mapValues { (_, deferred) -> deferred.await() }
                        completedArtwork to iconTask.await()
                    }
                } else {
                    emptyMap<Long, Pair<String, String>>() to if (downloadArtwork && iconUrl.isNotBlank()) {
                        download(context, retroAchievementsGameId, resolvedGameTitle, "icon.png", iconUrl)
                    } else ""
                }
                var badgeOk = 0
                var badgeFailed = 0
                items.indices.forEach { index ->
                    val item = items[index]
                    val paths = artwork[item.id]
                    val badge = paths?.first?.takeIf { it.isNotBlank() } ?: item.badgePath
                    val locked = paths?.second?.takeIf { it.isNotBlank() } ?: item.lockedBadgePath
                    if (badge.isNotBlank()) badgeOk++ else badgeFailed++
                    items[index] = item.copy(badgePath = badge, lockedBadgePath = locked)
                }
                if (iconPath.isNotBlank() && File(iconPath).isFile) {
                    runCatching { File(iconPath).copyTo(File(gameDir(context, retroAchievementsGameId, resolvedGameTitle), "logo.png"), overwrite = true) }
                        .onFailure { Timber.w(it, "Could not save RetroAchievements game logo") }
                }
                write(context, retroAchievementsGameId, items)
                val r = SyncResult(
                    success = true,
                    httpStatus = status,
                    gameId = retroAchievementsGameId,
                    achievementsReturned = items.size,
                    earnedReturned = earned,
                    badgesDownloaded = badgeOk,
                    badgesFailed = badgeFailed,
                    iconDownloaded = iconPath.isNotBlank(),
                    gameTitle = resolvedGameTitle,
                    responseFormat = "Achievements object",
                    nativePreparationBody = body,
                )
                writeDiagnostic(context, r, libraryGameId, romHash)
                r
            }
        }.getOrElse { error ->
            val r = SyncResult(false, gameId = retroAchievementsGameId, error = error.javaClass.simpleName)
            writeDiagnostic(context, r, libraryGameId, romHash)
            r
        }
        if (result.success && mirrorExport) exportToSelectedDataFolder(context)
        result
    }

    suspend fun exportToSelectedDataFolder(context: Context) {
        try {
            DirectoriesManager(context.applicationContext).exportAchievementsToSelectedDataFolder()
        } catch (error: Exception) {
            Timber.w(error, "Could not mirror achievements to the selected data folder")
        }
    }

    fun latestDiagnostic(context: Context, raGameId: Int): JSONObject? =
        readJson(File(gameDir(context, raGameId), DIAGNOSTICS_FILE))

    private fun lookupGameIdByRomHash(context: Context, systemId: String, romHash: String, title: String, apiKey: String): GameHashLookup {
        val consoleId = when (systemId.lowercase()) {
            "gb" -> 4
            "gba" -> 5
            "gbc" -> 6
            else -> return GameHashLookup(error = "unsupported_system")
        }
        val cacheFile = File(context.cacheDir, "ra-game-hashes/console-$consoleId.json")
        val cached = runCatching { JSONObject(cacheFile.readText()) }.getOrNull()
        val cachedGames = cached?.optJSONArray("games")
        val cacheFresh = cachedGames != null &&
            System.currentTimeMillis() - cached.optLong("fetchedAt") < GAME_HASH_CACHE_TTL_MS
        var games = if (cacheFresh) cachedGames else null
        var requestError = ""
        if (games == null) {
            val query = "y=${encode(apiKey)}&i=$consoleId&h=1&f=1"
            val request = Request.Builder().url("$BASE_URL/API/API_GetGameList.php?$query")
                .header("User-Agent", "OMINOY/1.18.0").get().build()
            runCatching {
                http.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        requestError = if (response.code == 429) "rate_limited" else "http_${response.code}"
                        return@use
                    }
                    val parsed = JSONArray(response.body?.string().orEmpty())
                    games = parsed
                    cacheFile.parentFile?.mkdirs()
                    atomicWrite(cacheFile, JSONObject().put("fetchedAt", System.currentTimeMillis()).put("games", parsed).toString())
                }
            }.onFailure { requestError = it.javaClass.simpleName }
            if (games == null) games = cachedGames
        }
        val gameList = games ?: return GameHashLookup(error = requestError.ifBlank { "game_hash_list_unavailable" })
        val matches = (0 until gameList.length()).mapNotNull { index ->
            val item = gameList.optJSONObject(index) ?: return@mapNotNull null
            val hashes = item.optJSONArray("Hashes") ?: item.optJSONArray("hashes") ?: return@mapNotNull null
            if ((0 until hashes.length()).any { hashes.optString(it).equals(romHash, ignoreCase = true) }) {
                val id = item.optInt("ID", item.optInt("id"))
                if (id > 0) id to item.optString("Title", item.optString("title")) else null
            } else null
        }
        if (matches.isEmpty()) return GameHashLookup(error = requestError.ifBlank { "no_hash_match" })
        fun normalize(value: String) = value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim().replace(Regex("\\s+"), " ")
        val normalizedTitle = normalize(title)
        val chosen = matches.firstOrNull { normalize(it.second) == normalizedTitle } ?: matches.first()
        return GameHashLookup(chosen.first, chosen.second)
    }

    private const val GAME_HASH_CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000

    suspend fun syncLocalGame(
        context: Context,
        libraryGameId: Int,
        fileUri: String,
        systemId: String,
        gameTitle: String,
        username: String,
        apiKey: String,
        onArtworkProgress: ((completed: Int, total: Int) -> Unit)? = null,
    ): LocalGameSyncResult = withContext(Dispatchers.IO) {
        val input = runCatching { context.contentResolver.openInputStream(Uri.parse(fileUri)) }
            .getOrNull() ?: return@withContext LocalGameSyncResult(false, error = "ROM cannot be opened")
        val hash = runCatching {
            val consoleId = raConsoleId(systemId) ?: return@runCatching ""
            RetroAchievementsNativeBridge.generateRetroAchievementsHash(input.use { it.readBytes() }, consoleId).orEmpty()
        }.getOrElse { return@withContext LocalGameSyncResult(false, error = it.javaClass.simpleName) }
            .takeIf { it.isNotBlank() }
            ?: return@withContext LocalGameSyncResult(false, error = "unsupported_system")
        val lookup = lookupGameIdByRomHash(context, systemId, hash, gameTitle, apiKey)
        if (lookup.gameId <= 0) return@withContext LocalGameSyncResult(false, error = lookup.error)
        val sync = syncFromServer(
            context = context,
            libraryGameId = libraryGameId,
            retroAchievementsGameId = lookup.gameId,
            username = username,
            apiKey = apiKey,
            romHash = hash,
            gameTitle = lookup.gameTitle.ifBlank { gameTitle },
            mirrorExport = false,
            downloadArtwork = true,
            onArtworkProgress = onArtworkProgress,
        )
        if (sync.success && sync.nativePreparationBody.isNotBlank()) {
            RetroAchievementsNativeBridge.prepareOfflineGame(
                context = context,
                romHash = hash,
                gameId = lookup.gameId,
                webApiBody = sync.nativePreparationBody,
                username = username,
            )
        }
        LocalGameSyncResult(sync.success, sync = sync, error = sync.error)
    }

    private fun raConsoleId(systemId: String): Int? = when (systemId.lowercase()) {
        "gb" -> 4
        "gba" -> 5
        "gbc" -> 6
        else -> null
    }

    /** Prepares the native rcheevos response cache for a title-matched library game. */
    suspend fun prepareOfflineGameForLibraryGame(
        context: Context,
        fileUri: String,
        systemId: String,
        retroAchievementsGameId: Int,
        webApiBody: String,
        username: String,
    ): Boolean = withContext(Dispatchers.IO) {
        if (retroAchievementsGameId <= 0 || webApiBody.isBlank() || username.isBlank()) return@withContext false
        val input = runCatching { context.contentResolver.openInputStream(Uri.parse(fileUri)) }
            .getOrNull() ?: return@withContext false
        val hash = runCatching {
            val consoleId = raConsoleId(systemId) ?: return@runCatching ""
            RetroAchievementsNativeBridge.generateRetroAchievementsHash(input.use { it.readBytes() }, consoleId).orEmpty()
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: return@withContext false
        RetroAchievementsNativeBridge.prepareOfflineGame(
            context = context,
            romHash = hash,
            gameId = retroAchievementsGameId,
            webApiBody = webApiBody,
            username = username,
        )
        true
    }

    private fun write(context: Context, raGameId: Int, values: Collection<Achievement>) {
        synchronized(writeLock) {
            val target = metadataFile(context, raGameId)
            val merged = readFile(target).associateBy { it.id }.toMutableMap()
            val userTarget = accountEarnedFile(context, raGameId)
            val userMerged = readFile(userTarget).associateBy { it.id }.toMutableMap()
            values.forEach { value ->
                val old = merged[value.id]
                val oldUser = userMerged[value.id]
                merged[value.id] = value.copy(
                    badgeUrl = value.badgeUrl.ifBlank { old?.badgeUrl.orEmpty() },
                    badgePath = value.badgePath.ifBlank { old?.badgePath.orEmpty() },
                    lockedBadgePath = value.lockedBadgePath.ifBlank { old?.lockedBadgePath.orEmpty() },
                    earned = false,
                    earnedAt = 0L,
                    hardcoreEarnedAt = 0L,
                    badgeName = value.badgeName.ifBlank { old?.badgeName.orEmpty() },
                    localLibraryGameId = value.localLibraryGameId.takeIf { it > 0 } ?: old?.localLibraryGameId ?: 0,
                    romHash = value.romHash.ifBlank { old?.romHash.orEmpty() },
                    gameTitle = value.gameTitle.ifBlank { old?.gameTitle.orEmpty() },
                )
                userMerged[value.id] = value.copy(
                    earned = value.earned || oldUser?.earned == true,
                    earnedAt = value.earnedAt.takeIf { it > 0L } ?: oldUser?.earnedAt ?: 0L,
                    hardcoreEarnedAt = value.hardcoreEarnedAt.takeIf { it > 0L } ?: oldUser?.hardcoreEarnedAt ?: 0L,
                )
            }
            val sortedValues = merged.values.sortedBy { it.id }
            val array = JSONArray()
            sortedValues.forEach { value -> array.put(toJson(value)) }
            atomicWrite(target, array.toString())
            atomicWrite(userTarget,
                JSONArray().apply { userMerged.values.filter { it.earned }.forEach { put(toJson(it)) } }.toString(),
            )
        }
    }

    private fun writeDiagnostic(context: Context, result: SyncResult, libraryGameId: Int, romHash: String) {
        runCatching {
            val objectValue = JSONObject().apply {
                put("gameId", result.gameId).put("localLibraryGameId", libraryGameId).put("romHash", romHash)
                put("httpStatus", result.httpStatus).put("success", result.success).put("error", result.error)
                put("responseFormat", result.responseFormat).put("achievementsReturned", result.achievementsReturned)
                put("earnedReturned", result.earnedReturned).put("badgesDownloaded", result.badgesDownloaded)
                put("badgesFailed", result.badgesFailed).put("iconDownloaded", result.iconDownloaded)
                put("lastSyncTime", System.currentTimeMillis()).put("localWorkingPath", gameDir(context, result.gameId).absolutePath)
            }
            atomicWrite(File(gameDir(context, result.gameId), DIAGNOSTICS_FILE), objectValue.toString())
        }.onFailure { Timber.w(it, "Could not write RetroAchievements sync diagnostics") }
    }

    private fun download(context: Context, raGameId: Int, gameTitle: String, name: String, url: String): String {
        if (url.isBlank()) return ""
        val dir = File(gameDir(context, raGameId, gameTitle), "badges").apply { mkdirs() }
        val target = File(dir, name)
        if (target.isFile && target.length() > 0) return target.absolutePath
        return runCatching {
            http.newCall(Request.Builder().url(url).header("User-Agent", "OMINOY/1.18.0").build()).execute().use { response ->
                if (!response.isSuccessful) return@use ""
                val bytes = response.body?.bytes() ?: byteArrayOf()
                if (bytes.isEmpty()) return@use ""
                val temp = File(target.parentFile, ".${target.name}.download")
                temp.writeBytes(bytes)
                if (!temp.renameTo(target)) temp.copyTo(target, overwrite = true)
                temp.delete()
                target.absolutePath
            }
        }.getOrElse { error ->
            Timber.w(error, "RetroAchievements artwork download failed")
            ""
        }
    }

    private fun mediaUrl(path: String): String = when {
        path.isBlank() -> ""
        path.startsWith("http") -> path
        else -> "$MEDIA_URL/${path.trimStart('/')}"
    }

    private fun parseDate(value: String): Long = runCatching { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(value)?.time ?: 0L }.getOrDefault(0L)
    private fun encode(value: String) = URLEncoder.encode(value, Charsets.UTF_8.name())
    private fun atomicWrite(target: File, text: String) {
        runCatching {
            target.parentFile?.mkdirs()
            val temporary = File(target.parentFile, ".${target.name}.tmp")
            try {
                java.io.FileOutputStream(temporary).use { output ->
                    output.write(text.toByteArray(Charsets.UTF_8))
                    output.fd.sync()
                }
                if (!temporary.renameTo(target)) throw IOException("Could not atomically replace ${target.name}")
            } finally {
                temporary.delete()
            }
        }.onFailure { Timber.w(it, "Could not write ${target.name}") }
    }
    private fun readFile(file: File): List<Achievement> =
        runCatching { parseArray(file.takeIf { it.isFile }?.readText() ?: "[]") }
            .onFailure { Timber.w(it, "Could not read ${file.name}") }
            .getOrDefault(emptyList())
    private fun parseArray(text: String, libraryGameId: Int = 0): List<Achievement> = runCatching {
        val array = JSONArray(text)
        (0 until array.length()).mapNotNull { index -> array.optJSONObject(index)?.let { fromJson(it, libraryGameId) } }
    }.getOrDefault(emptyList())
    private fun readJson(file: File): JSONObject? = runCatching { if (file.isFile) JSONObject(file.readText()) else null }.getOrNull()

    private fun toJson(v: Achievement) = JSONObject().apply {
        put("id", v.id).put("title", v.title).put("description", v.description).put("points", v.points)
        put("badgeUrl", v.badgeUrl).put("badgePath", v.badgePath).put("lockedBadgePath", v.lockedBadgePath)
        put("earned", v.earned).put("earnedAt", v.earnedAt).put("hardcoreEarnedAt", v.hardcoreEarnedAt)
        put("raGameId", v.raGameId).put("badgeName", v.badgeName).put("localLibraryGameId", v.localLibraryGameId).put("romHash", v.romHash)
        put("gameTitle", v.gameTitle)
    }
    private fun fromJson(v: JSONObject, libraryGameId: Int = 0) = Achievement(
        id = v.optLong("id"), title = v.optString("title", "Achievement"), description = v.optString("description"),
        points = v.optInt("points"), badgeUrl = v.optString("badgeUrl"), badgePath = v.optString("badgePath"),
        earned = v.optBoolean("earned"), earnedAt = v.optLong("earnedAt"), raGameId = v.optInt("raGameId"),
        lockedBadgePath = v.optString("lockedBadgePath"), badgeName = v.optString("badgeName"),
        hardcoreEarnedAt = v.optLong("hardcoreEarnedAt"), localLibraryGameId = v.optInt("localLibraryGameId", libraryGameId), romHash = v.optString("romHash"),
        gameTitle = v.optString("gameTitle"),
    )
}
