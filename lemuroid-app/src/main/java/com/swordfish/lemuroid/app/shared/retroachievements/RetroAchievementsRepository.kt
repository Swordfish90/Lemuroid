package com.swordfish.lemuroid.app.shared.retroachievements

import android.content.Context
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AchievementDetail(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String,
    val isUnlocked: Boolean,
    val unlockDate: String? = null,
    val isHardcore: Boolean = false,
)

data class GameAchievementsData(
    val gameId: Int,
    val title: String,
    val unlockedCount: Int,
    val totalCount: Int,
    val unlockedPoints: Int,
    val totalPoints: Int,
    val badgeIconUrl: String,
    val achievements: List<AchievementDetail>,
) {
    val completionPercentage: Float
        get() = if (totalCount > 0) (unlockedCount.toFloat() / totalCount.toFloat()) * 100f else 0f
}

sealed interface AchievementsUiState {
    object Disabled : AchievementsUiState
    object Loading : AchievementsUiState
    object NoAchievements : AchievementsUiState
    data class Success(val data: GameAchievementsData) : AchievementsUiState
    data class Error(val message: String) : AchievementsUiState
}

class RetroAchievementsRepository(private val context: Context) {

    private val prefs = RetroAchievementsPreferences(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "RetroArch/1.19.0 (Android) rcheevos/11.7.0")
                .build()
            chain.proceed(request)
        }
        .build()

    fun getAchievementsFlow(game: Game, crc32: String? = null): Flow<AchievementsUiState> = flow {
        if (!prefs.enabled) {
            emit(AchievementsUiState.Disabled)
            return@flow
        }

        emit(AchievementsUiState.Loading)

        val keys = listOfNotNull(
            game.fileName.takeIf { it.isNotBlank() },
            game.title.takeIf { it.isNotBlank() },
            game.fileUri.takeIf { it.isNotBlank() },
            crc32?.takeIf { it.isNotBlank() },
        )

        var gameId = prefs.getCachedGameId(*keys.toTypedArray())

        if (gameId != null && gameId > 0) {
            val cachedData = loadFromDiskCache(gameId)
            if (cachedData != null && cachedData.totalCount > 0) {
                emit(AchievementsUiState.Success(cachedData))
            }
        }

        if (gameId == null || gameId <= 0) {
            gameId = resolveGameIdFromRom(game)
            if (gameId != null && gameId > 0) {
                prefs.setCachedGameId(keys, gameId)
            }
        }

        if (gameId == null || gameId <= 0) {
            Timber.w("RetroAchievements: Game ID could not be resolved for ${game.title}")
            emit(AchievementsUiState.NoAchievements)
            return@flow
        }

        try {
            val username = prefs.username
            val token = prefs.token
            val hasCredentials = username.isNotBlank() && token.isNotBlank()

            val patchForm = FormBody.Builder()
                .add("r", "patch")
                .add("g", gameId.toString())
            if (hasCredentials) {
                patchForm.add("u", username)
                patchForm.add("t", token)
            }

            val patchRequest = Request.Builder()
                .url("https://retroachievements.org/dorequest.php")
                .post(patchForm.build())
                .build()

            val patchBody = client.newCall(patchRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                }
                response.body?.string() ?: ""
            }

            val unlockedMap = mutableMapOf<Int, Long>()
            val hardcoreSet = mutableSetOf<Int>()

            if (hasCredentials) {
                try {
                    val sessionForm = FormBody.Builder()
                        .add("r", "startsession")
                        .add("g", gameId.toString())
                        .add("u", username)
                        .add("t", token)
                        .build()

                    val sessionRequest = Request.Builder()
                        .url("https://retroachievements.org/dorequest.php")
                        .post(sessionForm)
                        .build()

                    client.newCall(sessionRequest).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val unlockJson = JSONObject(resp.body?.string() ?: "{}")
                            val unlocksArr = unlockJson.optJSONArray("Unlocks") ?: unlockJson.optJSONArray("UserUnlocks")
                            if (unlocksArr != null) {
                                for (i in 0 until unlocksArr.length()) {
                                    val item = unlocksArr.optJSONObject(i)
                                    if (item != null) {
                                        val id = item.optInt("ID", 0)
                                        val whenTs = item.optLong("When", 0L)
                                        if (id > 0) unlockedMap[id] = whenTs
                                    } else {
                                        val id = unlocksArr.optInt(i, 0)
                                        if (id > 0) unlockedMap[id] = 0L
                                    }
                                }
                            }
                            val hardcoreArr = unlockJson.optJSONArray("HardcoreUnlocks")
                            if (hardcoreArr != null) {
                                for (i in 0 until hardcoreArr.length()) {
                                    val item = hardcoreArr.optJSONObject(i)
                                    val id = item?.optInt("ID", 0) ?: hardcoreArr.optInt(i, 0)
                                    if (id > 0) hardcoreSet.add(id)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e, "RetroAchievements: Could not fetch user unlocks for game $gameId")
                }
            }

            val parsed = parseAchievementsJson(gameId, patchBody, unlockedMap, hardcoreSet)
            if (parsed != null && parsed.totalCount > 0) {
                saveToDiskCache(gameId, patchBody, unlockedMap, hardcoreSet)
                emit(AchievementsUiState.Success(parsed))
            } else {
                emit(AchievementsUiState.NoAchievements)
            }
        } catch (e: Exception) {
            Timber.e(e, "RetroAchievements: Failed to fetch game achievements for ID $gameId")
            val fallback = loadFromDiskCache(gameId)
            if (fallback != null && fallback.totalCount > 0) {
                emit(AchievementsUiState.Success(fallback))
            } else {
                emit(AchievementsUiState.Error(e.message ?: "Network error"))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun parseAchievementsJson(
        gameId: Int,
        jsonString: String,
        unlockedMap: Map<Int, Long> = emptyMap(),
        hardcoreSet: Set<Int> = emptySet(),
    ): GameAchievementsData? {
        return try {
            val root = JSONObject(jsonString)
            val patchData = root.optJSONObject("PatchData") ?: root
            val title = patchData.optString("Title", root.optString("Title", "Game #$gameId"))
            val iconPath = patchData.optString("ImageIcon", root.optString("ImageIcon", ""))
            val badgeIconUrl = if (iconPath.startsWith("http")) {
                iconPath
            } else if (iconPath.isNotBlank()) {
                "https://media.retroachievements.org$iconPath"
            } else {
                ""
            }

            val achievementsList = mutableListOf<AchievementDetail>()
            val achievementsObj = patchData.optJSONObject("Achievements") ?: root.optJSONObject("Achievements")
            val achievementsArr = patchData.optJSONArray("Achievements") ?: root.optJSONArray("Achievements")

            if (achievementsObj != null) {
                val keys = achievementsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = achievementsObj.optJSONObject(key) ?: continue
                    parseAchievementItem(item, unlockedMap, hardcoreSet)?.let { achievementsList.add(it) }
                }
            } else if (achievementsArr != null) {
                for (i in 0 until achievementsArr.length()) {
                    val item = achievementsArr.optJSONObject(i) ?: continue
                    parseAchievementItem(item, unlockedMap, hardcoreSet)?.let { achievementsList.add(it) }
                }
            }

            if (achievementsList.isEmpty()) {
                return null
            }

            val sorted = achievementsList.sortedWith(
                compareByDescending<AchievementDetail> { it.isUnlocked }
                    .thenBy { it.id }
            )

            val unlockedCount = sorted.count { it.isUnlocked }
            val unlockedPoints = sorted.filter { it.isUnlocked }.sumOf { it.points }
            val totalPoints = sorted.sumOf { it.points }

            GameAchievementsData(
                gameId = gameId,
                title = title,
                unlockedCount = unlockedCount,
                totalCount = sorted.size,
                unlockedPoints = unlockedPoints,
                totalPoints = totalPoints,
                badgeIconUrl = badgeIconUrl,
                achievements = sorted,
            )
        } catch (e: Exception) {
            Timber.e(e, "RetroAchievements: JSON parsing error")
            null
        }
    }

    private fun parseAchievementItem(
        item: JSONObject,
        unlockedMap: Map<Int, Long>,
        hardcoreSet: Set<Int>,
    ): AchievementDetail? {
        val flags = item.optInt("Flags", 3)
        if (flags != 3) {
            return null
        }

        val id = item.optInt("ID", item.optString("ID", "0").toIntOrNull() ?: 0)
        if (id == 0) return null
        val title = item.optString("Title", "Achievement #$id")
        val desc = item.optString("Description", "")
        val points = item.optInt("Points", item.optString("Points", "0").toIntOrNull() ?: 0)
        val badgeName = item.optString("BadgeName", "")
        val badgeUrl = if (badgeName.isNotBlank()) {
            "https://media.retroachievements.org/Badge/$badgeName.png"
        } else {
            ""
        }

        val unlockTimestamp = unlockedMap[id]
        val isUnlocked = unlockTimestamp != null
        val isHardcore = hardcoreSet.contains(id)
        val formattedDate = unlockTimestamp?.takeIf { it > 0 }?.let { ts ->
            try {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ts * 1000L))
            } catch (e: Exception) {
                null
            }
        }

        return AchievementDetail(
            id = id,
            title = title,
            description = desc,
            points = points,
            badgeUrl = badgeUrl,
            isUnlocked = isUnlocked,
            unlockDate = formattedDate,
            isHardcore = isHardcore,
        )
    }

    private fun resolveGameIdFromRom(game: Game): Int? {
        return try {
            val uri = android.net.Uri.parse(game.fileUri)
            val hashes = computeRomMd5Hashes(uri, game.fileName, game.systemId)
            Timber.d("RetroAchievements: Computed ROM MD5 hashes for '${game.title}': $hashes")

            for (hash in hashes) {
                val url = "https://retroachievements.org/dorequest.php?r=gameid&m=$hash"
                val req = Request.Builder().url(url).build()
                val id = client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val json = JSONObject(body)
                        val gameId = json.optInt("GameID", json.optString("GameID", "0").toIntOrNull() ?: 0)
                        if (gameId > 0) gameId else null
                    } else null
                }
                if (id != null && id > 0) {
                    Timber.i("RetroAchievements: Successfully resolved game ID $id for '${game.title}' using hash $hash")
                    return id
                }
            }
            null
        } catch (e: Exception) {
            Timber.w(e, "RetroAchievements: ROM hash resolution error for ${game.title}")
            null
        }
    }

    private fun computeRomMd5Hashes(uri: android.net.Uri, fileName: String, systemId: String): List<String> {
        val result = mutableListOf<String>()
        val isZipped = fileName.endsWith(".zip", ignoreCase = true)

        try {
            val rawBytes: ByteArray? = if (isZipped) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    java.util.zip.ZipInputStream(stream).use { zip ->
                        var entry = zip.nextEntry
                        var romData: ByteArray? = null
                        while (entry != null) {
                            val ext = entry.name.substringAfterLast('.', "").lowercase()
                            val isRom = ext in setOf("nes", "sfc", "smc", "gb", "gbc", "gba", "md", "smd", "gen", "bin", "pce", "ngp", "ngc", "ws", "wsc")
                            if (!entry.isDirectory && isRom) {
                                romData = zip.readBytes()
                                break
                            }
                            entry = zip.nextEntry
                        }
                        romData
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }

            if (rawBytes != null && rawBytes.isNotEmpty()) {
                val md = MessageDigest.getInstance("MD5")
                val isNesHeader = rawBytes.size > 16 &&
                    rawBytes[0] == 0x4E.toByte() &&
                    rawBytes[1] == 0x45.toByte() &&
                    rawBytes[2] == 0x53.toByte() &&
                    rawBytes[3] == 0x1A.toByte()

                if (isNesHeader) {
                    val strippedMd5 = md5Of(rawBytes, 16, rawBytes.size - 16)
                    result.add(strippedMd5)
                }

                if (systemId.equals("snes", ignoreCase = true) && rawBytes.size % 1024 == 512 && rawBytes.size > 512) {
                    val strippedMd5 = md5Of(rawBytes, 512, rawBytes.size - 512)
                    result.add(strippedMd5)
                }

                result.add(md5Of(rawBytes, 0, rawBytes.size))
            }
        } catch (e: Exception) {
            Timber.w(e, "RetroAchievements: Failed to read ROM bytes for hashing")
        }

        return result.distinct()
    }

    private fun md5Of(data: ByteArray, offset: Int, length: Int): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(data, offset, length)
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun getCacheFile(gameId: Int): File {
        val dir = File(context.cacheDir, "retroachievements")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "game_$gameId.json")
    }

    private fun saveToDiskCache(
        gameId: Int,
        patchJson: String,
        unlockedMap: Map<Int, Long>,
        hardcoreSet: Set<Int>,
    ) {
        try {
            val cacheObj = JSONObject()
            cacheObj.put("patch", patchJson)
            val unlocksObj = JSONObject()
            unlockedMap.forEach { (id, ts) -> unlocksObj.put(id.toString(), ts) }
            cacheObj.put("unlocks", unlocksObj)
            val hardcoreArr = JSONArray()
            hardcoreSet.forEach { hardcoreArr.put(it) }
            cacheObj.put("hardcore", hardcoreArr)
            getCacheFile(gameId).writeText(cacheObj.toString())
        } catch (e: Exception) {
            Timber.w(e, "RetroAchievements: Failed to write disk cache for $gameId")
        }
    }

    private fun loadFromDiskCache(gameId: Int): GameAchievementsData? {
        return try {
            val file = getCacheFile(gameId)
            if (file.exists() && file.length() > 0) {
                val content = file.readText()
                if (content.startsWith("{") && content.contains("\"patch\"")) {
                    val root = JSONObject(content)
                    val patchJson = root.getString("patch")
                    val unlocksObj = root.optJSONObject("unlocks")
                    val unlockedMap = mutableMapOf<Int, Long>()
                    unlocksObj?.keys()?.forEach { k ->
                        k.toIntOrNull()?.let { id -> unlockedMap[id] = unlocksObj.getLong(k) }
                    }
                    val hardcoreArr = root.optJSONArray("hardcore")
                    val hardcoreSet = mutableSetOf<Int>()
                    if (hardcoreArr != null) {
                        for (i in 0 until hardcoreArr.length()) {
                            hardcoreSet.add(hardcoreArr.getInt(i))
                        }
                    }
                    parseAchievementsJson(gameId, patchJson, unlockedMap, hardcoreSet)
                } else {
                    parseAchievementsJson(gameId, content)
                }
            } else null
        } catch (e: Exception) {
            Timber.w(e, "RetroAchievements: Failed to read disk cache for $gameId")
            null
        }
    }
}
