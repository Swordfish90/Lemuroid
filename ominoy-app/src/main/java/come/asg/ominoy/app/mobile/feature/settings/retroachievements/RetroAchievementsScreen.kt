package come.asg.ominoy.app.mobile.feature.settings.retroachievements

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import come.asg.ominoy.app.shared.game.RetroAchievementsEarnedStore
import com.swordfish.libretrodroid.RetroAchievementsNativeBridge
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import come.asg.ominoy.lib.library.db.entity.Game
import come.asg.ominoy.app.mobile.shared.compose.ui.RetroAchievementsColors as RaColors
import kotlinx.coroutines.launch

private const val RA_BASE_URL = "https://retroachievements.org/"
private const val LEGACY_KEY_ALIAS = "ominoy_ra_credentials"
private const val KEY_ALIAS = "ominoy_ra_credentials_v2"
private const val PREFS_NAME = "retro_achievements_credentials"

private val RaOn = RaColors.Text
private val RaMuted = RaColors.Muted
private val RaTeal = RaColors.Mint
private val RaGold = RaColors.Gold
private val RaCard = RaColors.Card
private val RaCard2 = RaColors.CardVariant
private val RaLine = RaColors.Line

private data class Credentials(val username: String, val token: String)
data class AchievementStats(
    val total: Int = 0,
    val unlocked: Int = 0,
    val unlockedHardcore: Int = 0,
    val games: List<AchievementGameProgress> = emptyList(),
)
data class AchievementGameProgress(
    val gameId: Int,
    val title: String,
    val consoleName: String,
    val total: Int,
    val unlocked: Int,
    val unlockedHardcore: Int,
)
data class Profile(
    val username: String,
    val points: String,
    val softcorePoints: String,
    val unreadMessages: String,
    val accountType: String,
    val avatarUrl: String?,
    val achievements: AchievementStats? = null,
    val achievementsLoading: Boolean = false,
    val webApiConnected: Boolean = false,
    val webApiMessage: String? = null,
)

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class LoggedIn(val profile: Profile) : AuthState
    data class Error(val message: String, val canRetry: Boolean = true) : AuthState
}

private object InvalidCredentialsException : IOException()
private object AuthRejectedException : IOException()
private object SavedCredentialsException : IOException()
private object ServerException : Exception()
private object SessionExpiredException : IOException()

private class EncryptedCredentials(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(LEGACY_KEY_ALIAS)) keyStore.deleteEntry(LEGACY_KEY_ALIAS)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    private fun generateKey() {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setKeySize(256)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build(),
            )
            generateKey()
        }
    }

    private fun rotateKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
        generateKey()
    }

    private fun key(): SecretKey =
        (KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            .getKey(KEY_ALIAS, null) as SecretKey)

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size)).toString(Charsets.UTF_8)
    }

    fun save(credentials: Credentials) {
        prefs.edit().putString("username", encrypt(credentials.username))
            .putString("token", encrypt(credentials.token)).commit()
    }

    fun saveWebApiKey(username: String, apiKey: String) {
        prefs.edit().putString("web_api_username", encrypt(username))
            .putString("web_api_key", encrypt(apiKey)).commit()
    }

    fun readWebApiCredentials(): RetroAchievementsWebApiCredentials? {
        val username = prefs.getString("web_api_username", null)
        val apiKey = prefs.getString("web_api_key", null)
        if (username.isNullOrBlank() || apiKey.isNullOrBlank()) return null
        return runCatching {
            RetroAchievementsWebApiCredentials(decrypt(username), decrypt(apiKey))
        }.getOrNull()
    }

    fun clearWebApiKey() {
        prefs.edit().remove("web_api_username").remove("web_api_key").commit()
    }

    fun read(): Credentials? {
        val username = prefs.getString("username", null)
        val token = prefs.getString("token", null)
        if (username == null && token == null) return null
        if (username.isNullOrBlank() || token.isNullOrBlank()) throw SavedCredentialsException
        return try {
            Credentials(decrypt(username), decrypt(token))
        } catch (error: Exception) {
            runCatching { rotateKey() }
            throw SavedCredentialsException
        }
    }

    fun clear() = prefs.edit().clear().commit()
}

/** Reads the existing encrypted token in the isolated emulator process; passwords are never shared. */
internal fun readGameplayCredentials(context: Context): RetroAchievementsSessionCredentials? =
    runCatching {
        EncryptedCredentials(context.applicationContext).read()?.let {
            RetroAchievementsSessionCredentials(it.username, it.token)
        }
    }.getOrNull()

internal fun readWebApiCredentials(context: Context): RetroAchievementsWebApiCredentials? =
    runCatching { EncryptedCredentials(context.applicationContext).readWebApiCredentials() }.getOrNull()

private class RetroAchievementsClient(private val credentials: EncryptedCredentials) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun request(endpoint: String, params: Map<String, String>): JSONObject =
        suspendCancellableCoroutine { continuation ->
            val url = (RA_BASE_URL + endpoint).toHttpUrl()
            val body = okhttp3.FormBody.Builder().apply {
                params.forEach { (key, value) -> add(key, value) }
            }.build()
            val call = httpClient.newCall(
                Request.Builder()
                    .url(url)
                    .header("User-Agent", "OMINOY/1.18.0")
                    .post(body)
                    .build(),
            )
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(
                object : Callback {
                    override fun onFailure(call: Call, error: IOException) {
                        if (continuation.isActive) continuation.resumeWith(Result.failure(error))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val result: Result<JSONObject> = runCatching {
                            response.use {
                                val responseBody = it.body?.string().orEmpty()
                                if (it.code == 401) throw AuthRejectedException
                                if (!it.isSuccessful) throw ServerException
                                JSONObject(responseBody)
                            }
                        }
                        if (continuation.isActive) continuation.resumeWith(result)
                    }
                },
            )
        }

    private suspend fun requestWebApi(endpoint: String, params: Map<String, String>): JSONObject =
        suspendCancellableCoroutine { continuation ->
            val urlBuilder = ("https://retroachievements.org/API/$endpoint").toHttpUrl().newBuilder()
            params.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value) }
            val call = httpClient.newCall(
                Request.Builder().url(urlBuilder.build()).header("User-Agent", "OMINOY/1.18.0").get().build(),
            )
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, error: IOException) {
                    if (continuation.isActive) continuation.resumeWith(Result.failure(error))
                }
                override fun onResponse(call: Call, response: Response) {
                    val result = runCatching {
                        response.use {
                            if (it.code == 401 || it.code == 403) throw AuthRejectedException
                            if (!it.isSuccessful) throw ServerException
                            JSONObject(it.body?.string().orEmpty())
                        }
                    }
                    if (continuation.isActive) continuation.resumeWith(result)
                }
            })
        }

    suspend fun validateWebApiKey(username: String, apiKey: String): Boolean {
        val response = requestWebApi("API_GetUserProfile.php", mapOf("y" to apiKey, "u" to username))
        return response.optString("User", username).equals(username, ignoreCase = true) ||
            response.has("ID") || response.has("MemberSince")
    }

    fun saveWebApiKey(username: String, apiKey: String) = credentials.saveWebApiKey(username, apiKey)
    fun readWebApiCredentials() = credentials.readWebApiCredentials()
    fun clearWebApiKey() = credentials.clearWebApiKey()

    suspend fun login(username: String, password: String): Profile =
        loadProfile(authenticate(username, mapOf("p" to password), saveCredentials = true))

    suspend fun restore(): Profile? {
        val saved = credentials.read() ?: return null
        return loadProfile(authenticate(saved.username, mapOf("t" to saved.token), saveCredentials = false))
    }

    suspend fun retryAchievementStats(): AchievementStats {
        val saved = credentials.read() ?: throw SessionExpiredException
        return fetchAchievementStats(saved.username, saved.token)
    }

    private suspend fun loadProfile(credentialsAndProfile: CredentialsAndProfile): Profile {
        val stats = try {
            fetchAchievementStats(credentialsAndProfile.username, credentialsAndProfile.token)
        } catch (error: AuthRejectedException) {
            throw error
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            null
        }
        return credentialsAndProfile.profile.copy(achievements = stats, achievementsLoading = false)
    }

    private suspend fun fetchAchievementStats(username: String, token: String): AchievementStats {
        var total = 0
        var unlocked = 0
        var hardcore = 0
        val gameProgress = mutableListOf<AchievementGameProgress>()
        // OMINOY supports these three RetroAchievements systems.
        for (consoleId in listOf(4, 5, 6)) {
            val result = request(
                "dorequest.php",
                mapOf("r" to "allprogress", "u" to username, "t" to token, "c" to consoleId.toString()),
            )
            val games = result.optJSONObject("Response") ?: throw ServerException
            games.keys().forEach { gameId ->
                val game = games.optJSONObject(gameId) ?: return@forEach
                val gameTotal = game.optInt("Achievements", 0)
                val gameUnlocked = game.optInt("Unlocked", 0)
                val gameHardcore = game.optInt("UnlockedHardcore", 0)
                total += gameTotal
                unlocked += gameUnlocked
                hardcore += gameHardcore
                val resolvedGameId = game.optInt("GameID", gameId.toIntOrNull() ?: 0)
                if (resolvedGameId > 0) {
                    val consoleName = game.optString("ConsoleName").ifBlank {
                        when (consoleId) {
                            4 -> "Game Boy"
                            5 -> "Game Boy Advance"
                            6 -> "Game Boy Color"
                            else -> "Console $consoleId"
                        }
                    }
                    val title = game.optString("Title")
                        .ifBlank { game.optString("GameTitle") }
                        .ifBlank { game.optString("GameName") }
                        .ifBlank { game.optString("Name") }
                        .ifBlank { "Game $resolvedGameId" }
                    gameProgress += AchievementGameProgress(
                        gameId = resolvedGameId,
                        title = title,
                        consoleName = consoleName,
                        total = gameTotal,
                        unlocked = gameUnlocked,
                        unlockedHardcore = gameHardcore,
                    )
                }
            }
        }
        return AchievementStats(total, unlocked, hardcore, gameProgress)
    }

    private data class CredentialsAndProfile(
        val username: String,
        val token: String,
        val profile: Profile,
    )

    private suspend fun authenticate(
        username: String,
        credential: Map<String, String>,
        saveCredentials: Boolean,
    ): CredentialsAndProfile {
        val result = try {
            request("dorequest.php", buildMap {
                put("r", "login2")
                put("u", username)
                putAll(credential)
            })
        } catch (error: AuthRejectedException) {
            if (saveCredentials) throw InvalidCredentialsException
            throw SessionExpiredException
        }
        if (!result.optBoolean("Success", false) || result.optString("Token").isBlank()) {
            if (saveCredentials) throw InvalidCredentialsException
            throw SessionExpiredException
        }
        val token = result.getString("Token")
        val returnedUser = result.optString("User", username)
        if (saveCredentials) credentials.save(Credentials(returnedUser, token))
        val avatar = result.optString("AvatarUrl").takeIf { it.isNotBlank() }
            ?: "https://retroachievements.org/UserPic/$returnedUser.png"
        return CredentialsAndProfile(
            returnedUser,
            token,
            Profile(
                username = returnedUser,
                points = result.optString("Score", "0"),
                softcorePoints = result.optString("SoftcoreScore", "0"),
                unreadMessages = result.optString("Messages", "0"),
                accountType = result.optString("AccountType", "Registered"),
                avatarUrl = avatar,
            ),
        )
    }

    fun logout() = credentials.clear()

    fun sessionCredentials(): RetroAchievementsSessionCredentials? =
        runCatching { credentials.read()?.let { RetroAchievementsSessionCredentials(it.username, it.token) } }
            .getOrNull()

    fun webApiCredentials(): RetroAchievementsWebApiCredentials? = credentials.readWebApiCredentials()
}

class RetroAchievementsViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    // Android Keystore initialization can take noticeable time on some devices;
    // create the client only inside the existing IO restore/login coroutines.
    private val client by lazy { RetroAchievementsClient(EncryptedCredentials(appContext)) }
    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()
    private val loginInFlight = AtomicBoolean(false)
    private val achievementRefreshInFlight = AtomicBoolean(false)

    init { restore() }

    private fun restore() {
        RetroAchievementsAccountBridge.profile.value?.let { activeProfile ->
            _state.value = AuthState.LoggedIn(activeProfile.copy(webApiConnected = RetroAchievementsAccountBridge.webApiCredentials.value != null))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            RetroAchievementsProfileCache.read(appContext)?.let { cachedProfile ->
                RetroAchievementsAccountBridge.setProfile(cachedProfile)
                runCatching { client.sessionCredentials()?.let(RetroAchievementsAccountBridge::set) }
                client.webApiCredentials()
                    ?.takeIf { it.username.equals(cachedProfile.username, true) }
                    ?.let(RetroAchievementsAccountBridge::setWebApiCredentials)
                _state.value = AuthState.LoggedIn(cachedProfile.copy(webApiConnected = client.webApiCredentials()?.username.equals(cachedProfile.username, true)))
                return@launch
            }
            _state.value = try {
                withTimeout(15_000L) {
                    client.restore()?.let { profile ->
                        rememberProfile(profile)
                        AuthState.LoggedIn(profile.copy(webApiConnected = RetroAchievementsAccountBridge.webApiCredentials.value != null))
                    } ?: AuthState.LoggedOut
                }
            } catch (error: SavedCredentialsException) {
                expireSession()
            } catch (error: SessionExpiredException) {
                expireSession()
            } catch (error: AuthRejectedException) {
                expireSession()
            } catch (error: TimeoutCancellationException) {
                mapError(SocketTimeoutException("RetroAchievements restore timed out"), true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mapError(error, true)
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Enter your username and password.")
            return
        }
        if (_state.value is AuthState.Loading || !loginInFlight.compareAndSet(false, true)) return
        val previousUsername = (_state.value as? AuthState.LoggedIn)?.profile?.username
        if (!previousUsername.equals(username.trim(), ignoreCase = true)) {
            RetroAchievementsWebSession.clearForLogout(appContext)
            RetroAchievementsNativeBridge.invalidateOfflineCache(appContext)
        }
        _state.value = AuthState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = client.login(username.trim(), password)
                val web = client.webApiCredentials()?.takeIf { value -> value.username.equals(profile.username, true) }
                if (web == null) client.clearWebApiKey()
                RetroAchievementsAccountBridge.setWebApiCredentials(web)
                rememberProfile(profile)
                _state.value = AuthState.LoggedIn(profile.copy(webApiConnected = web != null))
            } catch (error: InvalidCredentialsException) {
                _state.value = AuthState.Error("Incorrect username or password.")
            } catch (error: AuthRejectedException) {
                _state.value = expireSession()
            } catch (error: SavedCredentialsException) {
                _state.value = expireSession()
            } catch (error: SessionExpiredException) {
                _state.value = expireSession()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _state.value = mapError(error, false)
            } finally {
                loginInFlight.set(false)
            }
        }
    }

    fun retryAchievementStats() {
        val loggedIn = _state.value as? AuthState.LoggedIn ?: return
        if (loggedIn.profile.achievementsLoading || !achievementRefreshInFlight.compareAndSet(false, true)) return
        val pendingProfile = loggedIn.profile.copy(
            achievementsLoading = true,
            webApiMessage = "Refreshing account progress and missing game details…",
        )
        _state.value = AuthState.LoggedIn(pendingProfile)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stats = client.retryAchievementStats()
                val webCredentials = client.webApiCredentials()
                    ?.takeIf { it.username.equals(pendingProfile.username, ignoreCase = true) }
                var syncedGames = 0
                var failedGames = 0
                val resolvedTitles = mutableMapOf<Int, String>()
                var syncMessage = if (webCredentials == null) {
                    "Connect a Web API key to sync full per-game achievement lists."
                } else {
                    "Account progress refreshed."
                }

                if (webCredentials != null) {
                    val cachedByGame = runCatching { RetroAchievementsEarnedStore.readAll(appContext) }
                        .getOrDefault(emptyList())
                        .filter { it.raGameId > 0 }
                        .groupBy { it.raGameId }
                    val candidates = stats.games.asSequence()
                        .filter { it.total > 0 && it.unlocked > 0 }
                        .filter { game ->
                            val cached = cachedByGame[game.gameId].orEmpty()
                            cached.size < game.total || cached.count { it.earned } != game.unlocked ||
                                cached.any { it.badgePath.isBlank() || !File(it.badgePath).isFile }
                        }
                        .sortedByDescending { it.unlocked }
                        .take(3)
                        .toList()

                    var rateLimited = false
                    for (game in candidates) {
                        val result = try {
                            RetroAchievementsEarnedStore.syncFromServer(
                                context = appContext,
                                libraryGameId = 0,
                                retroAchievementsGameId = game.gameId,
                                username = webCredentials.username,
                                apiKey = webCredentials.apiKey,
                                gameTitle = game.title,
                                mirrorExport = false,
                                downloadArtwork = true,
                            )
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Throwable) {
                            null
                        }
                        if (result?.success == true) {
                            syncedGames++
                            result.gameTitle.takeUnless { it.isBlank() || it == "Game ${game.gameId}" }
                                ?.let { resolvedTitles[game.gameId] = it }
                        } else {
                            failedGames++
                            if (result?.deferredByRateLimit == true) {
                                rateLimited = true
                                break
                            }
                        }
                    }
                    if (syncedGames > 0) RetroAchievementsEarnedStore.exportToSelectedDataFolder(appContext)
                    syncMessage = when {
                        candidates.isEmpty() -> "Account and cached game details are up to date."
                        rateLimited -> "Synced $syncedGames game set(s); the server rate-limited the rest. Refresh again later."
                        failedGames == 0 -> "Synced detailed achievements for $syncedGames game set(s)."
                        syncedGames > 0 -> "Synced $syncedGames game set(s); $failedGames could not be refreshed."
                        else -> "Account progress refreshed, but game details could not sync. Check the Web API key and connection."
                    }
                }
                val current = _state.value as? AuthState.LoggedIn
                if (current?.profile?.username == pendingProfile.username) {
                    val refreshedStats = if (webCredentials != null) {
                        stats.copy(games = stats.games.map { game ->
                            resolvedTitles[game.gameId]?.let { game.copy(title = it) } ?: game
                        })
                    } else stats
                    val refreshedProfile = current.profile.copy(
                        achievements = refreshedStats,
                        achievementsLoading = false,
                        webApiMessage = syncMessage,
                    )
                    rememberProfile(refreshedProfile)
                    _state.value = AuthState.LoggedIn(
                        refreshedProfile,
                    )
                }
            } catch (error: AuthRejectedException) {
                _state.value = expireSession()
            } catch (error: SavedCredentialsException) {
                _state.value = expireSession()
            } catch (error: SessionExpiredException) {
                _state.value = expireSession()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val current = _state.value as? AuthState.LoggedIn
                if (current?.profile?.username == pendingProfile.username) {
                    _state.value = AuthState.LoggedIn(
                        current.profile.copy(
                            achievementsLoading = false,
                            webApiMessage = "Refresh failed. Check your connection and try again.",
                        ),
                    )
                }
            } finally {
                achievementRefreshInFlight.set(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            client.logout()
            RetroAchievementsWebSession.clearForLogout(appContext)
            RetroAchievementsNativeBridge.invalidateOfflineCache(appContext)
            RetroAchievementsAccountBridge.clear()
            RetroAchievementsProfileCache.clear(appContext)
            _state.value = AuthState.LoggedOut
        }
    }

    private fun rememberProfile(profile: Profile) {
        RetroAchievementsAccountBridge.setProfile(profile)
        RetroAchievementsProfileCache.save(appContext, profile)
        client.sessionCredentials()?.let(RetroAchievementsAccountBridge::set)
    }

    fun saveWebApiKey(apiKey: String) {
        val loggedIn = _state.value as? AuthState.LoggedIn ?: return
        if (apiKey.isBlank()) return
        _state.value = AuthState.LoggedIn(loggedIn.profile.copy(webApiMessage = "Validating Web API key…"))
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                if (!client.validateWebApiKey(loggedIn.profile.username, apiKey.trim())) throw AuthRejectedException
                client.saveWebApiKey(loggedIn.profile.username, apiKey.trim())
                RetroAchievementsWebApiCredentials(loggedIn.profile.username, apiKey.trim())
            }
            val current = _state.value as? AuthState.LoggedIn ?: return@launch
            result.onSuccess { web ->
                RetroAchievementsAccountBridge.setWebApiCredentials(web)
                _state.value = AuthState.LoggedIn(current.profile.copy(webApiConnected = true, webApiMessage = "API secured"))
            }.onFailure { error ->
                val message = when (error) {
                    is AuthRejectedException -> "Invalid Web API key"
                    is IOException -> "API unavailable"
                    else -> "Could not validate Web API key"
                }
                _state.value = AuthState.LoggedIn(current.profile.copy(webApiConnected = false, webApiMessage = message))
            }
        }
    }

    fun removeWebApiKey() {
        client.clearWebApiKey()
        RetroAchievementsAccountBridge.setWebApiCredentials(null)
        val current = _state.value as? AuthState.LoggedIn ?: return
        _state.value = AuthState.LoggedIn(current.profile.copy(webApiConnected = false, webApiMessage = "A Web API key is required for new synchronization."))
    }

    private fun expireSession(): AuthState {
        client.logout()
        RetroAchievementsWebSession.clearForLogout(appContext)
        RetroAchievementsNativeBridge.invalidateOfflineCache(appContext)
        RetroAchievementsAccountBridge.clear()
        return AuthState.Error("Session expired, please log in again.")
    }

    private fun mapError(error: Throwable, restoring: Boolean): AuthState = when (error) {
        is InvalidCredentialsException -> AuthState.Error("Incorrect username or password.")
        is SessionExpiredException, is SavedCredentialsException, is AuthRejectedException ->
            AuthState.Error("Session expired, please log in again.")
        is ServerException -> AuthState.Error("RetroAchievements is having issues. Try again later.")
        is SocketTimeoutException, is IOException -> AuthState.Error(
            if (restoring) "Can't reach RetroAchievements. Check your connection and try again."
            else "Can't reach RetroAchievements. Check your connection and try again.",
        )
        else -> AuthState.Error("RetroAchievements is having issues. Try again later.")
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RetroAchievementsViewModel(context) as T
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RetroAchievementsScreen(modifier: Modifier = Modifier, viewModel: RetroAchievementsViewModel, database: RetrogradeDatabase) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var webApiKey by remember { mutableStateOf("") }
    var webApiKeyVisible by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    var showKeyWebView by remember { mutableStateOf(false) }
    var keyWebView by remember { mutableStateOf<WebView?>(null) }
    var webPreviewMenuExpanded by remember { mutableStateOf(false) }
    var showWebApiDialog by rememberSaveable { mutableStateOf(false) }
    var showGames by remember { mutableStateOf(false) }
    var selectedGameId by remember { mutableStateOf<Int?>(null) }
    var unavailableGame by remember { mutableStateOf<CachedGameAchievements?>(null) }
    var selectedAchievement by remember { mutableStateOf<Pair<String, RetroAchievementsEarnedStore.Achievement>?>(null) }
    var cacheLoading by remember { mutableStateOf(false) }
    var libraryGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    var cachedAchievements by remember { mutableStateOf<List<RetroAchievementsEarnedStore.Achievement>>(emptyList()) }
    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun toast(message: String) { scope.launch { snackHost.showSnackbar(message, withDismissAction = false) } }
    val loggedIn = state as? AuthState.LoggedIn
    val profile = loggedIn?.profile
    val savedWebApiCredentials by RetroAchievementsAccountBridge.webApiCredentials.collectAsState()
    val hasSavedWebApiKey = profile != null &&
        savedWebApiCredentials?.username?.equals(profile.username, ignoreCase = true) == true
    val isWebApiVerifying = profile?.webApiMessage?.contains("Validating", ignoreCase = true) == true
    val webApiErrorMessage = profile?.webApiMessage?.takeIf { message ->
        !isWebApiVerifying &&
            (message == "Invalid Web API key" || message == "API unavailable" || message == "Could not validate Web API key")
    }
    LaunchedEffect(hasSavedWebApiKey) {
        if (hasSavedWebApiKey) {
            webApiKey = ""
            webApiKeyVisible = false
        }
    }
    val stats = profile?.achievements
    val refreshRotation by rememberInfiniteTransition(label = "accountRefresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "accountRefreshRotation",
    )

    LaunchedEffect(database) {
        libraryGames = withContext(Dispatchers.IO) { runCatching { database.gameDao().selectAll() }.getOrDefault(emptyList()) }
    }
    // Do not parse the complete achievement cache while the profile is opening.
    // It can contain thousands of rows and used to block the first composition.
    // Load it only when the user explicitly opens "Achievements by game".
    LaunchedEffect(loggedIn != null, showGames) {
        if (loggedIn != null && showGames) {
            password = ""
            cacheLoading = true
            cachedAchievements = withContext(Dispatchers.IO) { runCatching { RetroAchievementsEarnedStore.readAll(context) }.getOrDefault(emptyList()) }
            cacheLoading = false
        } else if (loggedIn == null) {
            cachedAchievements = emptyList()
            selectedGameId = null
        }
    }
    val games = remember(cachedAchievements, stats, libraryGames) {
        val ownedIds = libraryGames.mapTo(mutableSetOf()) { it.id }
        val byCache = cachedAchievements.filter { it.raGameId > 0 }.groupBy { it.raGameId }
        // Keep the profile responsive even for unusually large accounts. The full
        // cache is loaded lazily in the game-list dialog.
        val byAccount = stats?.games.orEmpty().take(200).associateBy { it.gameId }
        (byCache.keys + byAccount.keys).distinct().map { id ->
            val items = byCache[id].orEmpty()
            val account = byAccount[id]
            val title = account?.title?.takeUnless { it == "Game $id" }.orEmpty().ifBlank { items.firstOrNull()?.gameTitle.orEmpty() }.ifBlank { "Game $id" }
            val icon = items.asSequence().mapNotNull { a ->
                val path = a.badgePath.ifBlank { a.lockedBadgePath }
                path.takeIf { it.isNotBlank() }?.let { File(File(it).parentFile, "icon.png").takeIf(File::isFile)?.absolutePath }
            }.firstOrNull().orEmpty()
            val installed = items.any { it.localLibraryGameId in ownedIds } ||
                libraryGames.any { it.matchesRetroAchievementsTitle(title) }
            CachedGameAchievements(
                gameId = id, title = title, consoleName = account?.consoleName.orEmpty(), total = account?.total ?: items.size,
                unlocked = account?.unlocked ?: items.count { it.earned },
                unlockedHardcore = account?.unlockedHardcore ?: items.count { it.hardcoreEarnedAt > 0L },
                items = items.sortedWith(compareBy<RetroAchievementsEarnedStore.Achievement> { it.title.lowercase() }.thenBy { it.id }),
                iconPath = icon, isInstalled = installed,
            )
        }.filter { it.unlocked > 0 || it.items.any { achievement -> achievement.earned } }
            .sortedWith(compareByDescending<CachedGameAchievements> { it.unlocked }.thenBy { it.title.lowercase() })
    }
    val unlocked = stats?.unlocked ?: cachedAchievements.count { it.earned }
    val hardcore = stats?.unlockedHardcore ?: cachedAchievements.count { it.hardcoreEarnedAt > 0L }
    val available = stats?.total ?: cachedAchievements.size
    val progress = if (available > 0) (unlocked.toFloat() / available).coerceIn(0f, 1f) else 0f
    val selectedGame = games.firstOrNull { it.gameId == selectedGameId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackHost) },
    ) { insets ->
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp).padding(insets),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (state) {
                    AuthState.Loading -> Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = RaTeal)
                        Text("Connecting to RetroAchievements…", color = RaOn)
                    }
                    is AuthState.LoggedIn -> {
                        val p = profile!!
                        ProfileHero(
                            profile = p,
                            hasSavedKey = hasSavedWebApiKey,
                            onWebApiKeyClick = {
                                webApiKey = ""
                                webApiKeyVisible = false
                                showWebApiDialog = true
                            },
                        )
                        ProfileSection(title = "Achievement progress") {
                            Text("Game Boy, Game Boy Color and Game Boy Advance", style = MaterialTheme.typography.bodySmall, color = RaMuted)
                            if (p.achievementsLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = RaTeal)
                            else {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), strokeWidth = 8.dp, color = RaTeal, trackColor = Color.White.copy(alpha = .08f), strokeCap = StrokeCap.Round)
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${(progress * 100).toInt()}%", fontSize = 26.sp, fontWeight = FontWeight.Black, color = RaOn)
                                            Text("UNLOCKED", fontSize = 10.sp, color = RaMuted)
                                        }
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MockStat("Unlocked", unlocked.toString(), RaTeal)
                                        MockStat("Hardcore", hardcore.toString(), RaGold)
                                        MockStat("Available", available.toString(), RaColors.Outline)
                                    }
                                }
                                if (stats == null) Text("Progress totals are unavailable. Refresh to try again.", color = RaMuted)
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { showGames = true },
                            shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = RaCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RaLine),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Achievements by game", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = RaOn)
                                        Text("${games.size} games played · $unlocked unlocked", fontSize = 12.sp, color = RaMuted)
                                    }
                                    IconButton(enabled = !p.achievementsLoading, onClick = { viewModel.retryAchievementStats(); toast("Refreshing account progress…") }) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = if (p.achievementsLoading) "Refreshing account progress" else "Refresh account progress",
                                            tint = RaTeal,
                                            modifier = Modifier.graphicsLayer { rotationZ = if (p.achievementsLoading) refreshRotation else 0f },
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Open all games", tint = RaTeal)
                                }
                                when {
                                    cacheLoading && games.isEmpty() -> LinearProgressIndicator(Modifier.fillMaxWidth(), color = RaTeal)
                                    games.isEmpty() -> Text("No game progress found yet. Sync with a Web API key to browse game achievements.", color = RaMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
                                    else -> games.take(2).forEach { game -> GamePreviewRow(game, Modifier.padding(top = 8.dp)) }
                                }
                            }
                        }
                        ProfileSection(title = "Account") {
                            MockAccountRow("Username", p.username)
                            MockAccountRow("Account type", p.accountType)
                            MockAccountRow("Unread messages", p.unreadMessages)
                            Text("Your account token is encrypted and stored on this device.", color = RaMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
                            OutlinedButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://retroachievements.org/user/${Uri.encode(p.username)}"))) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(99.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = RaOn),
                            ) { Text("View profile on RetroAchievements") }
                            Button(
                                onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFB4AB), contentColor = RaColors.Error),
                            ) { Text("Log out") }
                        }
                    }
                    else -> {
                        ProfileSection(title = "Your retro journey") {
                            Text("Sign in to see your profile, achievement progress and game-by-game unlocks.", color = RaMuted)
                        }
                        ProfileSection(title = "Sign in") {
                            OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Show or hide password") } }, modifier = Modifier.fillMaxWidth())
                            if (state is AuthState.Error) Text((state as AuthState.Error).message, color = RaColors.Error)
                            Button(onClick = { viewModel.login(username, password) }, enabled = username.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
                        }
                        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://retroachievements.org"))) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Forgot your password or need an account?") }
                    }
                }
            }
        }
    }

    if (showWebApiDialog && profile != null) WebApiKeyDialog(
        username = profile.username,
        apiKey = webApiKey,
        onApiKeyChange = { webApiKey = it },
        hasSavedKey = hasSavedWebApiKey,
        isVerifying = isWebApiVerifying,
        errorMessage = webApiErrorMessage,
        isKeyVisible = webApiKeyVisible,
        onToggleKeyVisibility = { webApiKeyVisible = !webApiKeyVisible },
        onGetApiKey = { showKeyWebView = true },
        onSaveAndVerify = {
            viewModel.saveWebApiKey(webApiKey)
            toast("Validating Web API key…")
        },
        onResetAndEnterNewKey = {
            viewModel.removeWebApiKey()
            webApiKey = ""
            webApiKeyVisible = false
            toast("Web API key removed")
        },
        onDismiss = {
            showWebApiDialog = false
            webApiKey = ""
            webApiKeyVisible = false
        },
    )
    if (showKeyWebView && profile != null) Dialog(
        onDismissRequest = { showKeyWebView = false },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val applicationsUrl = "https://retroachievements.org/settings?tab=applications"
        fun openApplicationsInChrome() {
            val chromeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(applicationsUrl)).setPackage("com.android.chrome")
            runCatching { context.startActivity(chromeIntent) }
                .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(applicationsUrl))) }
        }
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().background(RaCard).padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = { webPreviewMenuExpanded = false; showKeyWebView = false; keyWebView = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close Web API preview", tint = RaOn)
                }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(22.dp)).background(RaCard2).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Text("retroachievements.org/settings", color = RaOn, fontSize = 13.sp, maxLines = 1)
                }
                Box {
                    IconButton(onClick = { webPreviewMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Web preview options", tint = RaOn)
                    }
                    DropdownMenu(
                        expanded = webPreviewMenuExpanded,
                        onDismissRequest = { webPreviewMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            onClick = {
                                webPreviewMenuExpanded = false
                                keyWebView?.reload()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Open in Chrome") },
                            onClick = {
                                webPreviewMenuExpanded = false
                                openApplicationsInChrome()
                            },
                        )
                    }
                }
            }
            HorizontalDivider(color = RaLine)
            AndroidView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                factory = { viewContext ->
                    RetroAchievementsWebSession.prepareForAccount(viewContext, profile.username)
                    WebView(viewContext).apply {
                        keyWebView = this
                        var openedApplicationsPage = false
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = "OMINOY Web API key setup"
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                val uri = runCatching { Uri.parse(url) }.getOrNull()
                                val host = uri?.host.orEmpty().lowercase()
                                val path = uri?.path.orEmpty().lowercase()
                                val isRa = host == "retroachievements.org" || host.endsWith(".retroachievements.org")
                                val isLoginPage = path.contains("login") || path.contains("signin")
                                if (isRa && !isLoginPage && !openedApplicationsPage) {
                                    openedApplicationsPage = true
                                    view.loadUrl(applicationsUrl)
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val host = request.url.host.orEmpty().lowercase()
                                return if (host == "retroachievements.org" || host.endsWith(".retroachievements.org")) {
                                    false
                                } else true
                            }
                        }
                        loadUrl("https://retroachievements.org/login")
                    }
                },
                onRelease = { webView ->
                    webView.stopLoading()
                    webView.destroy()
                    if (keyWebView === webView) keyWebView = null
                    webPreviewMenuExpanded = false
                },
            )
        }
    }
    if (showGames) Dialog(
        onDismissRequest = { showGames = false },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        GamesOverlay(
            games,
            onBack = { showGames = false },
            onGame = { game ->
                showGames = false
                if (game.isInstalled) selectedGameId = game.gameId else unavailableGame = game
            },
        )
    }
    if (selectedGame != null) Dialog(
        onDismissRequest = { selectedGameId = null },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        GameDetailOverlay(selectedGame!!, onBack = { selectedGameId = null }, onAchievement = { selectedAchievement = selectedGame!!.title to it })
    }
    if (selectedAchievement != null) AchievementDetailOverlay(
        gameTitle = selectedAchievement!!.first,
        achievement = selectedAchievement!!.second,
        onDismiss = { selectedAchievement = null },
    )
    if (unavailableGame != null) UnavailableGameDialog(
        game = unavailableGame!!,
        onDismiss = { unavailableGame = null },
        onProfile = {
            unavailableGame = null
            profile?.username?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://retroachievements.org/user/${Uri.encode(it)}"))) }
        },
    )
    if (confirmLogout) AlertDialog(
        onDismissRequest = { confirmLogout = false }, title = { Text("Log out of RetroAchievements?") },
        text = { Text("Your saved RetroAchievements token will be removed from this device.") },
        dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } },
        confirmButton = { TextButton(onClick = { confirmLogout = false; viewModel.logout(); toast("Logged out") }) { Text("Log out") } },
    )
}

@Composable
private fun MockStat(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(label, color = RaMuted, fontSize = 13.sp)
        Text(value, color = RaOn, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun MockAccountRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = RaMuted, fontSize = 14.sp)
        Text(value.ifBlank { "—" }, color = RaOn, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    Divider(color = RaLine)
}

@Composable
private fun GamePreviewRow(game: CachedGameAchievements, modifier: Modifier = Modifier) {
    val pct = if (game.total > 0) (game.unlocked * 100 / game.total) else 0
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(RaCard2).padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GameCover(game, Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)))
        Column(Modifier.weight(1f)) {
            Text(game.title, color = RaOn, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${game.unlocked} of ${game.total} · $pct%", color = RaMuted, fontSize = 12.sp)
            if (!game.isInstalled) Text("Not installed", color = RaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameCover(game: CachedGameAchievements, modifier: Modifier = Modifier) {
    if (game.iconPath.isNotBlank() && File(game.iconPath).isFile) {
        AsyncImage(game.iconPath, contentDescription = "${game.title} cover", contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        Box(modifier.background(Brush.linearGradient(listOf(RaColors.Purple, RaColors.DeepPurple, RaColors.DeepTeal))), contentAlignment = Alignment.Center) {
            Text(game.title.take(3).uppercase(), color = RaOn, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun GamesOverlay(games: List<CachedGameAchievements>, onBack: () -> Unit, onGame: (CachedGameAchievements) -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).padding(top = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RaOn) }
            Text("All games", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RaOn)
        }
        Text("${games.size} games · ${games.sumOf { it.unlocked }} achievements unlocked", color = RaMuted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        if (games.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No games with achievement data yet.", color = RaMuted) }
        else LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games.size, key = { games[it].gameId }) { index ->
                val game = games[index]
                val pct = if (game.total > 0) game.unlocked * 100 / game.total else 0
                Card(Modifier.fillMaxWidth().clickable { onGame(game) }, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = RaCard2), border = androidx.compose.foundation.BorderStroke(1.dp, RaLine)) {
                    Column {
                        Box(Modifier.fillMaxWidth().height(132.dp).background(Brush.linearGradient(listOf(RaColors.Purple, RaColors.DeepPurple, RaColors.DeepTeal))), contentAlignment = Alignment.Center) {
                            if (game.iconPath.isNotBlank() && File(game.iconPath).isFile) AsyncImage(game.iconPath, contentDescription = "${game.title} cover", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) else Text(game.title.take(3).uppercase(), color = RaOn, fontSize = 34.sp, fontWeight = FontWeight.Black)
                            Text("$pct%", Modifier.align(Alignment.TopEnd).padding(8.dp).clip(RoundedCornerShape(99.dp)).background(Color.Black.copy(alpha = .7f)).padding(horizontal = 9.dp, vertical = 4.dp), color = if (game.unlocked > 0) RaTeal else RaMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(game.title, color = RaOn, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (game.isInstalled) shortSystem(game.consoleName) else "Not installed", color = if (game.isInstalled) RaMuted else RaGold, fontSize = 11.sp, fontWeight = if (game.isInstalled) FontWeight.Normal else FontWeight.Bold)
                                Text("${game.unlocked}/${game.total}", color = RaMuted, fontSize = 11.sp)
                            }
                            LinearProgressIndicator(progress = { if (game.total > 0) game.unlocked.toFloat() / game.total else 0f }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = RaTeal, trackColor = Color.White.copy(alpha = .12f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameDetailOverlay(game: CachedGameAchievements, onBack: () -> Unit, onAchievement: (RetroAchievementsEarnedStore.Achievement) -> Unit) {
    var tab by remember(game.gameId) { mutableStateOf(if (game.unlocked > 0) "Unlocked" else "Show all") }
    val unlocked = game.items.filter { it.earned }
    val locked = game.items.filterNot { it.earned }
    val shown = when (tab) { "Unlocked" -> unlocked; "Locked" -> locked; else -> game.items }
    Column(Modifier.fillMaxSize().background(Color.Black).padding(top = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RaOn) }
            Text(game.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RaOn, maxLines = 1)
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = RaCard), border = androidx.compose.foundation.BorderStroke(1.dp, RaLine)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        GameCover(game, Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)))
                        Column(Modifier.weight(1f)) {
                            Text(game.title, color = RaOn, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 2)
                            Text(game.consoleName, color = RaMuted, fontSize = 12.sp)
                            LinearProgressIndicator(progress = { if (game.total > 0) game.unlocked.toFloat() / game.total else 0f }, Modifier.fillMaxWidth().padding(top = 8.dp).height(5.dp).clip(CircleShape), color = RaTeal)
                            Text("${game.unlocked} of ${game.total} unlocked · ${if (game.total > 0) game.unlocked * 100 / game.total else 0}%", color = RaTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)).background(RaCard2).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("Unlocked" to unlocked.size, "Locked" to locked.size, "Show all" to game.items.size).forEach { (label, count) ->
                        TextButton(onClick = { tab = label }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp), colors = ButtonDefaults.textButtonColors(containerColor = if (tab == label) RaTeal else Color.Transparent, contentColor = if (tab == label) RaColors.OnMint else RaMuted)) { Text("$label ($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = RaCard), border = androidx.compose.foundation.BorderStroke(1.dp, RaLine)) {
                    if (shown.isEmpty()) Box(Modifier.fillMaxWidth().padding(26.dp), contentAlignment = Alignment.Center) { Text(if (tab == "Locked") "Everything is unlocked. Nice!" else "No achievements unlocked yet. Try the 'Show all' filter.", color = RaMuted, fontSize = 14.sp) }
                    else Column(Modifier.padding(horizontal = 14.dp)) {
                        shown.forEachIndexed { index, achievement ->
                            AchievementListItem(achievement, onClick = { onAchievement(achievement) }, mockupStyle = true)
                            if (index != shown.lastIndex) Divider(color = RaLine)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnavailableGameDialog(
    game: CachedGameAchievements,
    onDismiss: () -> Unit,
    onProfile: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(game.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GameCover(game, Modifier.size(84.dp).clip(RoundedCornerShape(20.dp)))
                Text("${game.unlocked} achievement${if (game.unlocked == 1) "" else "s"} earned", fontWeight = FontWeight.Bold)
                Text("This game is not available in your system. Download the game to see its full achievement list, or visit your RetroAchievements profile.")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        confirmButton = { TextButton(onClick = onProfile) { Text("View profile") } },
    )
}

private fun shortSystem(system: String): String = when (system.lowercase()) {
    "game boy" -> "GB"
    "game boy color" -> "GBC"
    "game boy advance" -> "GBA"
    "super nintendo" -> "SNES"
    "genesis" -> "Genesis"
    else -> system.ifBlank { "—" }
}

private fun Game.matchesRetroAchievementsTitle(raTitle: String): Boolean {
    val localVariants = listOf(title, displayName(), fileName.substringBeforeLast('.', fileName))
        .map(::normalizeGameTitle)
        .filter { it.length >= 4 }
    val ra = normalizeGameTitle(raTitle)
    if (ra.length < 4) return false
    return localVariants.any { local ->
        local == ra ||
            (minOf(local.length, ra.length) >= 7 && (local.contains(ra) || ra.contains(local))) ||
            run {
                val localTokens = local.split(' ').filter { it.length > 2 }.toSet()
                val raTokens = ra.split(' ').filter { it.length > 2 }.toSet()
                val common = localTokens.intersect(raTokens).size
                val smaller = minOf(localTokens.size, raTokens.size)
                smaller > 0 && common.toFloat() / smaller >= 0.75f
            }
    }
}

private fun normalizeGameTitle(value: String): String {
    val withoutTags = value
        .replace(Regex("\\([^)]*\\)|\\[[^]]*]"), " ")
        .replace(Regex("\\.(zip|7z|rar|nes|gb|gbc|gba|sfc|smc|bin|rom)$", RegexOption.IGNORE_CASE), " ")
    val normalized = java.text.Normalizer.normalize(withoutTags, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
    val ignored = setOf("usa", "europe", "japan", "world", "rev", "revision", "version", "proto", "beta", "the", "and")
    return normalized.split(' ').filterNot { it in ignored }.joinToString(" ")
}

private data class CachedGameAchievements(
    val gameId: Int,
    val title: String,
    val consoleName: String,
    val total: Int,
    val unlocked: Int,
    val unlockedHardcore: Int,
    val items: List<RetroAchievementsEarnedStore.Achievement>,
    val iconPath: String = "",
    val isInstalled: Boolean = false,
)

@Composable
private fun ProfileHero(profile: Profile, hasSavedKey: Boolean, onWebApiKeyClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(
            Brush.linearGradient(listOf(RaColors.Purple, RaColors.DeepPurple, RaColors.DeepTeal)),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (profile.avatarUrl != null) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "${profile.username}'s RetroAchievements avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(76.dp).clip(CircleShape),
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Account avatar", modifier = Modifier.size(76.dp), tint = Color(0xFFF4F1F8))
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                    Text("RETROACHIEVEMENTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RaOn.copy(alpha = 0.72f), letterSpacing = 1.sp)
                    Text(profile.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = RaOn)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("★ ${profile.accountType.uppercase()}", color = RaGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("● ONLINE", color = RaTeal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HeroPoints(label = "HARDCORE POINTS", value = profile.points, modifier = Modifier.weight(1.3f), valueColor = RaGold)
                HeroPoints(label = "SOFTCORE", value = profile.softcorePoints, modifier = Modifier.weight(1f), valueColor = RaOn)
            }
        }
        Box(Modifier.align(Alignment.TopEnd).padding(12.dp).size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.38f)).clickable(onClick = onWebApiKeyClick), contentAlignment = Alignment.Center) {
            Text("🔑", color = RaOn, fontSize = 17.sp)
            if (hasSavedKey) Box(Modifier.align(Alignment.TopEnd).padding(5.dp).size(9.dp).clip(CircleShape).background(RaTeal))
        }
    }
}

@Composable
private fun HeroPoints(label: String, value: String, modifier: Modifier, valueColor: Color) {
    Column(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.28f)).padding(14.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFCFC9DC))
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = valueColor, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ProfileMetricCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RaCard2),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = RaTeal, modifier = Modifier.size(22.dp))
            Text(value.ifBlank { "—" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = RaOn)
            Text(label, style = MaterialTheme.typography.labelMedium, color = RaMuted)
        }
    }
}

@Composable
private fun CompactStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RaOn)
        Text(label, style = MaterialTheme.typography.labelSmall, color = RaMuted)
    }
}

@Composable
private fun AchievementListItem(
    achievement: RetroAchievementsEarnedStore.Achievement,
    onClick: () -> Unit,
    mockupStyle: Boolean = false,
) {
    val badge = if (achievement.earned) achievement.badgePath else achievement.lockedBadgePath.ifBlank { achievement.badgePath }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).alpha(if (achievement.earned) 1f else .55f).padding(vertical = if (mockupStyle) 11.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = badge.takeIf { it.isNotBlank() && File(it).isFile },
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            contentDescription = "${achievement.title} badge",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(if (mockupStyle) 46.dp else 42.dp).clip(RoundedCornerShape(11.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(achievement.title, color = RaOn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(if (achievement.earned) "Unlocked" else "Locked", color = RaMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Text("${achievement.points} pts", color = if (achievement.earned) RaGold else RaMuted, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Icon(Icons.Default.ChevronRight, contentDescription = "Achievement details", tint = RaMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AchievementDetailOverlay(
    gameTitle: String,
    achievement: RetroAchievementsEarnedStore.Achievement,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)).pointerInput(onDismiss) {
            detectTapGestures(onTap = { onDismiss() })
        }, contentAlignment = Alignment.BottomCenter) {
            val interaction = remember { MutableInteractionSource() }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().widthIn(max = 680.dp)
                    .heightIn(max = maxHeight * .88f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(RaCard2)
                    .clickable(interactionSource = interaction, indication = null) { }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .2f)))
                AchievementDetailContent(gameTitle, achievement)
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@Composable
private fun AchievementDetailContent(gameTitle: String, achievement: RetroAchievementsEarnedStore.Achievement) {
    val badge = if (achievement.earned) achievement.badgePath else achievement.lockedBadgePath.ifBlank { achievement.badgePath }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AsyncImage(model = badge.takeIf { it.isNotBlank() && File(it).isFile }, contentDescription = "${achievement.title} badge", contentScale = ContentScale.Crop, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(26.dp)))
        Text(achievement.title, color = RaOn, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("$gameTitle · RetroAchievements", color = RaMuted, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("${achievement.points} pts") })
            AssistChip(onClick = {}, label = { Text(if (achievement.earned) "✓ Unlocked" else "🔒 Locked") }, colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(containerColor = if (achievement.earned) RaTeal.copy(alpha = .12f) else Color.White.copy(alpha = .07f)))
            if (achievement.hardcoreEarnedAt > 0L) AssistChip(onClick = {}, label = { Text("★ Hardcore") })
        }
        ProfileSection(title = "HOW TO EARN") { Text(achievement.description.ifBlank { "No description is available." }, color = RaOn, fontSize = 15.sp) }
        ProfileSection(title = if (achievement.earned) "UNLOCKED" else "STATUS") {
            if (achievement.earned) {
                val date = if (achievement.earnedAt > 0L) java.text.DateFormat.getDateInstance().format(java.util.Date(achievement.earnedAt * 1000L)) else "Date unavailable"
                Text("$date · ${if (achievement.hardcoreEarnedAt > 0L) "Hardcore" else "Softcore"}", color = RaOn, fontSize = 15.sp)
            } else Text("Not unlocked yet. Keep playing to earn ${achievement.points} points.", color = RaMuted, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = RaCard),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = RaOn,
            )
            HorizontalDivider(color = RaLine)
            content()
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = RaMuted,
        )
        Text(
            value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = RaOn,
            fontWeight = FontWeight.Medium,
        )
    }
}
