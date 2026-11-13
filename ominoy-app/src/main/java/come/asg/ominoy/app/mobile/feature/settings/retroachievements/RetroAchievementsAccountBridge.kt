package come.asg.ominoy.app.mobile.feature.settings.retroachievements

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Credentials used only by the native login2 gameplay client. */
data class RetroAchievementsSessionCredentials(
    val username: String,
    val token: String,
)

/** Credentials used only by OMINOY's documented Web API synchronization layer. */
data class RetroAchievementsWebApiCredentials(
    val username: String,
    val apiKey: String,
)

/**
 * Narrow in-memory bridge from the encrypted account screen to an active game session.
 * The token is cleared as soon as the account logs out or expires.
 */
object RetroAchievementsAccountBridge {
    private val _credentials = MutableStateFlow<RetroAchievementsSessionCredentials?>(null)
    val credentials: StateFlow<RetroAchievementsSessionCredentials?> = _credentials.asStateFlow()

    private val _webApiCredentials = MutableStateFlow<RetroAchievementsWebApiCredentials?>(null)
    val webApiCredentials: StateFlow<RetroAchievementsWebApiCredentials?> = _webApiCredentials.asStateFlow()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    fun set(credentials: RetroAchievementsSessionCredentials) {
        _credentials.value = credentials
    }

    fun setWebApiCredentials(credentials: RetroAchievementsWebApiCredentials?) {
        _webApiCredentials.value = credentials
    }

    fun setProfile(profile: Profile) {
        _profile.value = profile
    }

    fun clear() {
        _credentials.value = null
        _webApiCredentials.value = null
        _profile.value = null
    }
}

/** Lightweight local cache so the profile can render before the next background refresh. */
object RetroAchievementsProfileCache {
    private const val PREFS = "retro_achievements_profile_cache"
    private const val PROFILE = "profile"

    fun save(
        context: Context,
        profile: Profile,
    ) {
        val json =
            JSONObject()
                .put("username", profile.username)
                .put("points", profile.points)
                .put("softcorePoints", profile.softcorePoints)
                .put("unreadMessages", profile.unreadMessages)
                .put("accountType", profile.accountType)
                .put("avatarUrl", profile.avatarUrl)
        profile.achievements?.let { stats ->
            json.put(
                "achievements",
                JSONObject()
                    .put("total", stats.total)
                    .put("unlocked", stats.unlocked)
                    .put("unlockedHardcore", stats.unlockedHardcore)
                    .put(
                        "games",
                        JSONArray().apply {
                            stats.games.forEach { game ->
                                put(
                                    JSONObject()
                                        .put("gameId", game.gameId)
                                        .put("title", game.title)
                                        .put("consoleName", game.consoleName)
                                        .put("total", game.total)
                                        .put("unlocked", game.unlocked)
                                        .put("unlockedHardcore", game.unlockedHardcore),
                                )
                            }
                        },
                    ),
            )
        }
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(PROFILE, json.toString())
            .apply()
    }

    fun read(context: Context): Profile? =
        runCatching {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PROFILE, null) ?: return null
            val json = JSONObject(raw)
            val statsJson = json.optJSONObject("achievements")
            val stats =
                statsJson?.let {
                    val gamesJson = it.optJSONArray("games") ?: JSONArray()
                    AchievementStats(
                        total = it.optInt("total"),
                        unlocked = it.optInt("unlocked"),
                        unlockedHardcore = it.optInt("unlockedHardcore"),
                        games =
                            (0 until gamesJson.length()).mapNotNull { index ->
                                gamesJson.optJSONObject(index)?.let { game ->
                                    AchievementGameProgress(
                                        gameId = game.optInt("gameId"),
                                        title = game.optString("title"),
                                        consoleName = game.optString("consoleName"),
                                        total = game.optInt("total"),
                                        unlocked = game.optInt("unlocked"),
                                        unlockedHardcore = game.optInt("unlockedHardcore"),
                                    )
                                }
                            },
                    )
                }
            Profile(
                username = json.optString("username"),
                points = json.optString("points", "0"),
                softcorePoints = json.optString("softcorePoints", "0"),
                unreadMessages = json.optString("unreadMessages", "0"),
                accountType = json.optString("accountType", "Registered"),
                avatarUrl = json.optString("avatarUrl").takeIf { it.isNotBlank() },
                achievements = stats,
            )
        }.getOrNull()?.takeIf { it.username.isNotBlank() }

    fun clear(context: Context) {
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
