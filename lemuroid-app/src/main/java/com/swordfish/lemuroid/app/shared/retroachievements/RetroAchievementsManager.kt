package com.swordfish.lemuroid.app.shared.retroachievements

import android.content.Context
import com.swordfish.lemuroid.R
import com.swordfish.libretrodroid.LibretroDroid
import com.swordfish.libretrodroid.RetroAchievementsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

data class AchievementUnlockNotification(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String,
)

data class AchievementBannerNotification(
    val category: String,
    val categoryResId: Int = 0,
    val title: String,
    val subtitle: String,
    val badgeUrl: String,
    val badgeLabel: String,
)

data class AchievementProgressNotification(
    val id: Int,
    val title: String,
    val progress: String,
    val percent: Float,
)

object RetroAchievementsManager : RetroAchievementsListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private const val USER_AGENT = "RetroArch/1.19.0 (Android) rcheevos/11.7.0"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .build()
            chain.proceed(request)
        }
        .build()

    private val _unlockEvents = MutableSharedFlow<AchievementUnlockNotification>(extraBufferCapacity = 64)
    val unlockEvents = _unlockEvents.asSharedFlow()

    private val _bannerEvents = MutableSharedFlow<AchievementBannerNotification>(extraBufferCapacity = 64)
    val bannerEvents = _bannerEvents.asSharedFlow()

    private val _bannerState = MutableStateFlow<AchievementBannerNotification?>(null)
    val bannerFlow: StateFlow<AchievementBannerNotification?> = _bannerState.asStateFlow()

    fun showBanner(banner: AchievementBannerNotification) {
        _bannerState.value = banner
        _bannerEvents.tryEmit(banner)
    }

    fun dismissBanner(banner: AchievementBannerNotification) {
        if (_bannerState.value == banner) {
            _bannerState.value = null
        }
    }

    private val _progressEvents = MutableSharedFlow<AchievementProgressNotification>(extraBufferCapacity = 64)
    val progressEvents = _progressEvents.asSharedFlow()

    private val _statusMessages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val statusMessages = _statusMessages.asSharedFlow()

    private var appContext: Context? = null
    private var activePreferences: RetroAchievementsPreferences? = null
    private var currentGameKeys = listOf<String>()
    private var cachedBannerShown: Boolean = false
    private var cachedSummary: CachedGameSummary? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        activePreferences = RetroAchievementsPreferences(context.applicationContext)
    }

    fun login(context: Context, username: String, passwordOrToken: String, isPassword: Boolean) {
        appContext = context.applicationContext
        scope.launch(Dispatchers.IO) {
            val prefs = activePreferences ?: RetroAchievementsPreferences(context.applicationContext).also { activePreferences = it }
            LibretroDroid.initRetroAchievements(
                username,
                if (!isPassword) passwordOrToken else "",
                prefs.hardcore,
                this@RetroAchievementsManager,
            )
            LibretroDroid.loginRetroAchievements(username, passwordOrToken, isPassword)
        }
    }

    fun logout(context: Context? = null) {
        val targetContext = context?.applicationContext ?: appContext
        if (targetContext != null) {
            val prefs = RetroAchievementsPreferences(targetContext)
            prefs.username = ""
            prefs.token = ""
        }
        activePreferences?.let {
            it.username = ""
            it.token = ""
        }
        try {
            LibretroDroid.initRetroAchievements("", "", false, null)
        } catch (ignored: Throwable) {}
        val msg = (context ?: appContext)?.getString(R.string.ra_toast_logged_out) ?: "Logged out"
        _statusMessages.tryEmit(msg)
    }

    fun onGameStart(
        context: Context,
        systemId: String,
        gameTitle: String = "",
        gameFileName: String = "",
        gamePath: String = "",
    ) {
        appContext = context.applicationContext
        currentGameKeys = listOfNotNull(gameFileName, gameTitle, gamePath).filter { it.isNotEmpty() }
        cachedBannerShown = false
        cachedSummary = null

        val prefs = activePreferences ?: RetroAchievementsPreferences(context.applicationContext).also { activePreferences = it }
        if (!prefs.enabled) {
            Timber.i("RetroAchievements is disabled in preferences.")
            return
        }

        scope.launch(Dispatchers.IO) {
            val consoleId = getConsoleId(systemId)
            if (consoleId == 0) {
                Timber.w("No RetroAchievements console ID mapping for system: $systemId")
                return@launch
            }

            val cachedHash = prefs.getCachedGameHash(gameFileName) ?: prefs.getCachedGameHash(gamePath) ?: ""

            Timber.i("Initializing RetroAchievements for user: ${prefs.username}, hardcore: ${prefs.hardcore}, consoleId: $consoleId")
            LibretroDroid.initRetroAchievements(
                prefs.username,
                prefs.token,
                prefs.hardcore,
                this@RetroAchievementsManager,
            )

            LibretroDroid.loadRetroAchievementsGame(
                consoleId,
                cachedHash,
                gamePath,
            )
        }
    }

    fun getConsoleId(systemId: String): Int = when (systemId.lowercase()) {
        "nes" -> 7
        "snes" -> 3
        "gb" -> 4
        "gbc" -> 6
        "gba" -> 5
        "n64" -> 2
        "nds" -> 18
        "md", "genesis" -> 1
        "sms" -> 11
        "gg" -> 15
        "psx" -> 12
        "psp" -> 41
        "atari2600" -> 25
        "atari7800" -> 51
        "lynx" -> 13
        "pce", "pcecd" -> 8
        "fbneo", "mame2003plus", "arcade" -> 27
        "scd", "segacd" -> 9
        "ngp", "ngpc" -> 14
        "ws", "wsc" -> 53
        "dos" -> 26
        "3ds" -> 62
        else -> 0
    }

    override fun onLoginSuccess(username: String, token: String) {
        Timber.i("RetroAchievements logged in successfully as $username")
        val prefs = (appContext?.let { RetroAchievementsPreferences(it) }) ?: activePreferences
        prefs?.let {
            it.username = username
            if (token.isNotEmpty()) {
                it.token = token
            }
        }
        val msg = appContext?.getString(R.string.ra_toast_logged_in_as, username) ?: "Logged in as $username"
        _statusMessages.tryEmit(msg)
    }

    override fun onLoginFailure(error: String) {
        Timber.e("RetroAchievements login failed: $error")
        val msg = appContext?.getString(R.string.ra_toast_login_failed, error) ?: "RA Login Failed: $error"
        _statusMessages.tryEmit(msg)
    }

    override fun onGameLoaded(
        gameId: Int,
        title: String,
        unlockedAchievements: Int,
        totalAchievements: Int,
        unlockedPoints: Int,
        totalPoints: Int,
        badgeUrl: String,
    ) {
        Timber.i("RetroAchievements game loaded: ID=$gameId, Title=$title, Achievements=$unlockedAchievements/$totalAchievements ($unlockedPoints/$totalPoints pts)")
        _statusMessages.tryEmit("$title ($totalAchievements achievements)")

        if (currentGameKeys.isNotEmpty()) {
            activePreferences?.setCachedGameId(
                keys = currentGameKeys,
                gameId = gameId,
            )
            activePreferences?.setCachedGameSummary(
                keys = currentGameKeys,
                title = title,
                unlockedAchievements = unlockedAchievements,
                totalAchievements = totalAchievements,
                unlockedPoints = unlockedPoints,
                totalPoints = totalPoints,
                badgeUrl = badgeUrl,
            )
        }

        if (totalAchievements > 0) {
            val alreadyShown = cachedBannerShown
            val cached = cachedSummary
            if (alreadyShown && cached != null &&
                cached.title == title &&
                cached.unlockedAchievements == unlockedAchievements &&
                cached.totalAchievements == totalAchievements &&
                cached.unlockedPoints == unlockedPoints &&
                cached.totalPoints == totalPoints
            ) {
                Timber.i("Summary matches cached banner already displayed; skipping duplicate.")
                return
            }

            cachedSummary = CachedGameSummary(
                title = title,
                unlockedAchievements = unlockedAchievements,
                totalAchievements = totalAchievements,
                unlockedPoints = unlockedPoints,
                totalPoints = totalPoints,
                badgeUrl = badgeUrl,
            )

            val ctx = appContext
            val subtitleText = if (ctx != null) {
                if (unlockedAchievements > 0) {
                    ctx.getString(R.string.ra_achievements_summary_unlocked, unlockedAchievements, totalAchievements, unlockedPoints, totalPoints)
                } else {
                    ctx.getString(R.string.ra_achievements_summary_available, totalAchievements, totalPoints)
                }
            } else {
                if (unlockedAchievements > 0) {
                    "$unlockedAchievements of $totalAchievements Unlocked ($unlockedPoints/$totalPoints pts)"
                } else {
                    "$totalAchievements Achievements Available ($totalPoints pts)"
                }
            }
            val banner = AchievementBannerNotification(
                category = "RETROACHIEVEMENTS",
                categoryResId = R.string.ra_banner_category,
                title = title,
                subtitle = subtitleText,
                badgeUrl = badgeUrl,
                badgeLabel = "$unlockedAchievements/$totalAchievements",
            )
            showBanner(banner)
        }
    }

    override fun onGameLoadFailed(error: String) {
        Timber.w("RetroAchievements game load failed: $error")
    }

    override fun onAchievementUnlocked(
        id: Int,
        title: String,
        description: String,
        points: Int,
        badgeName: String,
        badgeUrl: String,
    ) {
        Timber.i("RetroAchievements Achievement Unlocked! ID=$id, Title='$title', Points=$points, Badge=$badgeName")
        val finalBadgeUrl = if (badgeUrl.isNotEmpty()) badgeUrl else "https://media.retroachievements.org/Badge/$badgeName.png"
        cachedSummary?.let { old ->
            val updated = old.copy(
                unlockedAchievements = old.unlockedAchievements + 1,
                unlockedPoints = old.unlockedPoints + points,
            )
            cachedSummary = updated
            if (currentGameKeys.isNotEmpty()) {
                activePreferences?.setCachedGameSummary(
                    keys = currentGameKeys,
                    title = updated.title,
                    unlockedAchievements = updated.unlockedAchievements,
                    totalAchievements = updated.totalAchievements,
                    unlockedPoints = updated.unlockedPoints,
                    totalPoints = updated.totalPoints,
                    badgeUrl = updated.badgeUrl,
                )
            }
        }

        val banner = AchievementBannerNotification(
            category = "ACHIEVEMENT UNLOCKED",
            categoryResId = R.string.ra_achievement_unlocked,
            title = title,
            subtitle = description,
            badgeUrl = finalBadgeUrl,
            badgeLabel = "+$points",
        )
        showBanner(banner)
        _unlockEvents.tryEmit(AchievementUnlockNotification(id, title, description, points, finalBadgeUrl))
    }

    override fun onAchievementProgress(
        id: Int,
        title: String,
        progress: String,
        percent: Float,
    ) {
        _progressEvents.tryEmit(AchievementProgressNotification(id, title, progress, percent))
    }

    override fun onGameMastered() {
        val msg = appContext?.getString(R.string.ra_game_mastered) ?: "Mastery! All achievements completed!"
        _statusMessages.tryEmit(msg)
    }

    override fun onServerRequest(
        url: String,
        postData: String?,
        contentType: String?,
        requestId: Long,
    ) {
        scope.launch {
            try {
                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                if (postData != null) {
                    val mediaType = (contentType ?: "application/x-www-form-urlencoded").toMediaTypeOrNull()
                    requestBuilder.post(postData.toRequestBody(mediaType))
                } else {
                    requestBuilder.get()
                }

                okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                    val statusCode = response.code
                    val body = response.body?.string() ?: ""
                    Timber.d("RetroAchievements HTTP response ($statusCode) for $url: ${body.take(120)}")
                    LibretroDroid.sendRetroAchievementsServerResponse(requestId, statusCode, body)
                }
            } catch (e: Exception) {
                Timber.e(e, "RetroAchievements HTTP call failed for url: $url")
                LibretroDroid.sendRetroAchievementsServerResponse(requestId, -1, "")
            }
        }
    }
}
