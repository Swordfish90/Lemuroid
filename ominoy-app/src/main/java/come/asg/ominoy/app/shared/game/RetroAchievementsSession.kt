package come.asg.ominoy.app.shared.game

import android.content.Context
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.RetroAchievementsNativeBridge
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsSessionCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readGameplayCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readWebApiCredentials
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest

sealed interface RetroAchievementsGameState {
    data object SignedOut : RetroAchievementsGameState
    data object Connecting : RetroAchievementsGameState
    data class Ready(val gameId: Int, val title: String, val mode: String = "Softcore") : RetroAchievementsGameState
    data class OfflineReady(val gameId: Int, val title: String, val mode: String = "Softcore") : RetroAchievementsGameState
    data object NoSetFound : RetroAchievementsGameState
    data class Offline(val canRetry: Boolean = true) : RetroAchievementsGameState
    data class Unsupported(val message: String) : RetroAchievementsGameState
    data class Error(val message: String, val canRetry: Boolean = true) : RetroAchievementsGameState
}

data class RetroAchievementPopup(
    val id: Long,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String,
    val overflowCount: Int = 0,
)

/** Owns the game-scoped RA session. Hashing and network work stay off the emulation thread. */
class RetroAchievementsSession(private val scope: CoroutineScope) {
    private val _state = MutableStateFlow<RetroAchievementsGameState>(RetroAchievementsGameState.SignedOut)
    val state: StateFlow<RetroAchievementsGameState> = _state.asStateFlow()
    private val _achievementPopup = MutableStateFlow<RetroAchievementPopup?>(null)
    val achievementPopup: StateFlow<RetroAchievementPopup?> = _achievementPopup.asStateFlow()
    private var eventJob: Job? = null
    private var startJob: Job? = null
    private var popupTimer: Job? = null
    private var consoleId: Int = 0
    private var libraryGameId: Int = 0
    private var retroAchievementsGameId: Int = 0
    private var romPath: String? = null
    private var romHash: String = ""
    private var gameView: GLRetroView? = null
    private var readyState: RetroAchievementsGameState.Ready? = null
    private val popupQueue = ArrayDeque<RetroAchievementPopup>()
    private var nextPopupId = 1L
    private var overflowCount = 0

    fun start(context: Context, consoleId: Int, romPath: String?, view: GLRetroView, libraryGameId: Int = 0) {
        stop()
        // Cache native RA responses in every build so a previously prepared game can
        // load its achievement set and continue earning casual achievements offline.
        RetroAchievementsNativeBridge.initializeOfflineCache(context)
        this.consoleId = consoleId
        this.libraryGameId = libraryGameId
        this.romPath = romPath
        this.gameView = view
        if (consoleId !in setOf(4, 5, 6)) {
            _state.value = RetroAchievementsGameState.Unsupported("RetroAchievements supports GB, GBC, and GBA only.")
            return
        }
        val credentials = readGameplayCredentials(context)
        if (credentials == null) {
            _state.value = RetroAchievementsGameState.SignedOut
            return
        }
        if (romPath == null) {
            _state.value = RetroAchievementsGameState.Unsupported("This ROM cannot be identified from a regular file.")
            return
        }

        _state.value = RetroAchievementsGameState.Connecting
        eventJob = scope.launch {
            RetroAchievementsNativeBridge.events.collect { event ->
                when (event.type) {
                    100 -> _state.value = RetroAchievementsGameState.Connecting
                    101 -> _state.value = RetroAchievementsGameState.Error(event.detail.ifBlank { "RetroAchievements login failed." })
                    102 -> {
                        val parts = event.detail.split('|', limit = 2)
                        val gameId = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        retroAchievementsGameId = gameId
                        val title = parts.getOrNull(1).orEmpty().ifBlank { "Game recognized" }
                        readyState = RetroAchievementsGameState.Ready(gameId, title)
                        _state.value = readyState!!
                        syncAchievementCache(view.context, credentials)
                    }
                    103 -> _state.value = RetroAchievementsGameState.Error(event.detail.ifBlank { "Could not load this game's achievement set." })
                    104 -> _state.value = RetroAchievementsGameState.NoSetFound
                    1 -> {
                        val cached = RetroAchievementsEarnedStore.findByRaGame(view.context, retroAchievementsGameId, event.achievementId)
                        val badgeUrl = event.badgeUrl.ifBlank { cached?.badgeUrl.orEmpty() }
                        val badgePath = cached?.badgePath.orEmpty()
                        RetroAchievementsEarnedStore.record(
                            view.context,
                            libraryGameId,
                            RetroAchievementsEarnedStore.Achievement(
                                id = event.achievementId,
                                title = event.title.ifBlank { "Achievement unlocked" },
                                description = event.detail,
                                points = event.points,
                                badgeUrl = badgeUrl,
                                badgePath = badgePath,
                                earned = true,
                                earnedAt = event.unlockTimeMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
                                raGameId = retroAchievementsGameId,
                                localLibraryGameId = libraryGameId,
                                gameTitle = readyState?.title.orEmpty(),
                            ),
                        )
                        RetroAchievementsPendingStore.enqueue(
                            view.context,
                            RetroAchievementsPendingStore.PendingUnlock(
                                username = credentials.username,
                                achievementId = event.achievementId,
                                rawRomMd5 = romHash,
                                raGameId = retroAchievementsGameId,
                                hardcore = readyState?.mode.equals("Hardcore", ignoreCase = true),
                                unlockTimeMillis = event.unlockTimeMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
                                localLibraryGameId = libraryGameId,
                                gameTitle = readyState?.title.orEmpty(),
                            ),
                        )
                        RetroAchievementsPendingUploadWork.enqueue(view.context)
                        scope.launch(Dispatchers.IO) {
                            RetroAchievementsEarnedStore.exportToSelectedDataFolder(view.context)
                        }
                        enqueueAchievement(event.title, event.detail, event.points, badgePath.ifBlank { badgeUrl })
                    }
                    16 -> _state.value = RetroAchievementsGameState.Error(event.detail.ifBlank { "RetroAchievements server error." })
                    17 -> {
                        _state.value = if (RetroAchievementsNativeBridge.isOfflineGameReady(view.context, romHash, retroAchievementsGameId)) {
                            RetroAchievementsGameState.OfflineReady(retroAchievementsGameId, readyState?.title ?: "Game")
                        } else RetroAchievementsGameState.Offline()
                    }
                    18 -> readyState?.let {
                        _state.value = it
                        syncAchievementCache(view.context, credentials)
                    }
                }
            }
        }
        startJob = scope.launch(Dispatchers.IO) {
            try {
                val hash = md5(File(romPath))
                this@RetroAchievementsSession.romHash = hash
                view.configureRetroAchievements(credentials.username, credentials.token, hash)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.value = RetroAchievementsGameState.Error("Could not read or identify this ROM.")
            }
        }
    }

    fun retry() {
        val path = romPath ?: return
        val view = gameView ?: return
        start(view.context.applicationContext, consoleId, path, view, libraryGameId)
    }

    fun stop() {
        startJob?.cancel()
        startJob = null
        eventJob?.cancel()
        eventJob = null
        popupTimer?.cancel()
        popupTimer = null
        popupQueue.clear()
        overflowCount = 0
        _achievementPopup.value = null
        readyState = null
        retroAchievementsGameId = 0
        romHash = ""
        _state.value = RetroAchievementsGameState.SignedOut
    }

    private fun syncAchievementCache(context: Context, credentials: RetroAchievementsSessionCredentials) {
        if (retroAchievementsGameId <= 0 || libraryGameId <= 0) return
        if (RetroAchievementsNativeBridge.isOfflineGameReady(context, romHash, retroAchievementsGameId) && !hasNetwork(context)) {
            _state.value = RetroAchievementsGameState.OfflineReady(retroAchievementsGameId, readyState?.title ?: "Game")
            return
        }
        val webCredentials = readWebApiCredentials(context) ?: return
        scope.launch(Dispatchers.IO) {
            val result = RetroAchievementsEarnedStore.syncFromServer(
                context = context,
                libraryGameId = libraryGameId,
                retroAchievementsGameId = retroAchievementsGameId,
                username = webCredentials.username,
                apiKey = webCredentials.apiKey,
                gameTitle = readyState?.title.orEmpty(),
            )
            if (!result.success && !result.deferredByRateLimit) {
                _state.value = RetroAchievementsGameState.Error("Achievement sync failed: ${result.error}")
            }
        }
    }

    private fun hasNetwork(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
        return manager.activeNetwork?.let { manager.getNetworkCapabilities(it) }
            ?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun enqueueAchievement(title: String, description: String, points: Int, badgeUrl: String) {
        val popup = RetroAchievementPopup(
            id = nextPopupId++,
            title = title.ifBlank { "Achievement unlocked" },
            description = description,
            points = points,
            badgeUrl = badgeUrl,
        )
        if (_achievementPopup.value == null) {
            showAchievement(popup)
        } else if (popupQueue.count { it.overflowCount == 0 } < 3 && overflowCount == 0) {
            popupQueue.addLast(popup)
        } else {
            overflowCount += 1
            if ((popupQueue.lastOrNull()?.overflowCount ?: 0) > 0) {
                popupQueue.removeLast()
            }
            popupQueue.addLast(
                RetroAchievementPopup(
                    id = nextPopupId++,
                    title = "+$overflowCount more achievements unlocked",
                    description = "",
                    points = 0,
                    badgeUrl = "",
                    overflowCount = overflowCount,
                ),
            )
        }
    }

    private fun showAchievement(popup: RetroAchievementPopup) {
        _achievementPopup.value = popup
        popupTimer?.cancel()
        popupTimer = scope.launch {
            delay(4_250L)
            _achievementPopup.value = null
            if (popupQueue.isNotEmpty()) {
                delay(300L)
                val next = popupQueue.removeFirst()
                if (next.overflowCount > 0) overflowCount = 0
                showAchievement(next)
            }
        }
    }

    private fun md5(file: File): String {
        require(file.isFile && file.canRead() && file.length() > 0L) { "ROM is unavailable" }
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
