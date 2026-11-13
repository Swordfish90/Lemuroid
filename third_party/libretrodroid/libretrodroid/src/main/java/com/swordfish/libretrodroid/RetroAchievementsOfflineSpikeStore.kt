package com.swordfish.libretrodroid

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject

/**
 * Durable persistence for offline RetroAchievements response replay and diagnostics.
 * The file is private to the app and encrypted with an Android Keystore AES key.
 * No token, password, or raw request URL is written to the inventory log.
 */
internal class RetroAchievementsOfflineSpikeStore(context: Context) {
    data class CachedResponse(val statusCode: Int, val body: String)

    private val file = File(context.applicationContext.filesDir, "ra_offline_spike.bin")
    private val keyAlias = "ominoy.ra.offline.spike.v1"
    private val lock = Any()
    private var cache: MutableMap<String, CachedResponse> = mutableMapOf()
    private var inventory: MutableList<String> = mutableListOf()
    private var unlocks: MutableList<String> = mutableListOf()
    private var loaded = false

    fun cachedResponse(key: String): CachedResponse? = synchronized(lock) {
        loadLocked()
        cache[hash(key)]
    }

    fun prepareGame(romHash: String, gameId: Int, webApiBody: String, username: String) = synchronized(lock) {
        loadLocked()
        if (romHash.isBlank() || gameId <= 0 || webApiBody.isBlank() || username.isBlank()) return@synchronized
        val normalizedRomHash = romHash.lowercase()
        val source = runCatching { JSONObject(webApiBody) }.getOrNull() ?: return@synchronized
        val achievements = source.optJSONObject("Achievements") ?: JSONObject()
        val achievementItems = JSONArray()
        val achievementKeys = achievements.keys()
        while (achievementKeys.hasNext()) {
            val key = achievementKeys.next()
            val item = achievements.optJSONObject(key) ?: continue
            val achievementId = item.optLong("ID", key.toLongOrNull() ?: 0L)
            if (achievementId <= 0L) continue
            // rc_api_runtime.c treats these fields as mandatory. Older
            // API_GetGameInfo responses can omit Flags and other metadata.
            item.put("ID", achievementId)
            item.put("Title", item.optString("Title").ifBlank { "Achievement $achievementId" })
            item.put("Description", item.optString("Description"))
            if (!item.has("Flags")) item.put("Flags", 3)
            if (!item.has("Points")) item.put("Points", 0)
            item.put("MemAddr", item.optString("MemAddr").ifBlank { item.optString("Mem").ifBlank { "0=0" } })
            item.put("Author", item.optString("Author"))
            item.put("BadgeName", item.optString("BadgeName").ifBlank { "00000" })
            if (!item.has("Created")) item.put("Created", 0)
            if (!item.has("Modified")) item.put("Modified", 0)
            achievementItems.put(item)
        }
        val title = source.optString("Title", "Game $gameId")
        val consoleId = source.optInt("ConsoleID", source.optInt("ConsoleId", 0))
        val imageIconUrl = source.optString("ImageIconUrl").ifBlank {
            source.optString("ImageIcon").let { icon ->
                if (icon.isBlank()) "" else if (icon.startsWith("http")) icon else "$MEDIA_URL/${icon.trimStart('/')}"
            }
        }
        val achievementSet = JSONObject()
            .put("AchievementSetId", gameId)
            .put("GameId", gameId)
            .put("Title", JSONObject.NULL)
            .put("Type", "core")
            .put("ImageIconUrl", imageIconUrl)
            .put("Achievements", achievementItems)
            .put("Leaderboards", JSONArray())
        val achievementSets = JSONObject()
            .put("Success", true)
            .put("GameId", gameId)
            .put("Title", title)
            .put("ConsoleId", consoleId)
            .put("ImageIconUrl", imageIconUrl)
            .put("RichPresenceGameId", gameId)
            .put("RichPresencePatch", source.optString("RichPresencePatch", ""))
            .put("Sets", JSONArray().put(achievementSet)).toString()
        val userProgress = source.optJSONObject("UserProgress") ?: JSONObject()
        val unlocks = JSONArray()
        val hardcoreUnlocks = JSONArray()
        val progressKeys = userProgress.keys()
        while (progressKeys.hasNext()) {
            val key = progressKeys.next()
            val item = userProgress.optJSONObject(key) ?: continue
            val achievementId = item.optLong("ID", key.toLongOrNull() ?: 0L)
            if (achievementId <= 0L) continue
            if (item.optString("DateEarned").isNotBlank()) {
                unlocks.put(JSONObject().put("ID", achievementId).put("When", 0))
            }
            if (item.optString("DateEarnedHardcore").isNotBlank()) {
                hardcoreUnlocks.put(JSONObject().put("ID", achievementId).put("When", 0))
            }
        }
        val unlocked = JSONObject().put("Success", true).put("GameID", gameId)
            .put("UserProgress", userProgress).toString()
        val session = JSONObject().put("Success", true).put("GameID", gameId)
            .put("Unlocks", unlocks).put("HardcoreUnlocks", hardcoreUnlocks).toString()
        val login = JSONObject().put("Success", true).put("User", username)
            .put("Token", "offline-local").put("Score", 0).put("SoftcoreScore", 0).put("Messages", 0).toString()
        cache[hash("prepared:login2")] = CachedResponse(200, login)
        cache[hash("prepared:$normalizedRomHash:gameid")] = CachedResponse(200, JSONObject()
            .put("Success", true).put("GameID", gameId).toString())
        cache[hash("prepared:$normalizedRomHash:achievementsets")] = CachedResponse(200, achievementSets)
        cache[hash("prepared:$gameId:achievementsets")] = CachedResponse(200, achievementSets)
        cache[hash("prepared:$gameId:unlocks")] = CachedResponse(200, unlocked)
        cache[hash("prepared:$gameId:startsession")] = CachedResponse(200, session)
        persistLocked()
    }

    fun preparedResponse(action: String, romHash: String?, gameId: Int?): CachedResponse? = synchronized(lock) {
        loadLocked()
        val normalizedRomHash = romHash?.lowercase()
        val key = when {
            action == "gameid" && !normalizedRomHash.isNullOrBlank() -> "prepared:$normalizedRomHash:gameid"
            action == "login2" -> "prepared:login2"
            action == "achievementsets" && !normalizedRomHash.isNullOrBlank() -> "prepared:$normalizedRomHash:achievementsets"
            action == "achievementsets" && gameId != null && gameId > 0 -> "prepared:$gameId:achievementsets"
            action == "unlocks" && gameId != null && gameId > 0 -> "prepared:$gameId:unlocks"
            action == "startsession" && gameId != null && gameId > 0 -> "prepared:$gameId:startsession"
            else -> return@synchronized null
        }
        cache[hash(key)]
    }

    fun isPrepared(romHash: String, gameId: Int): Boolean = synchronized(lock) {
        loadLocked()
        val normalizedRomHash = romHash.lowercase()
        normalizedRomHash.isNotBlank() && gameId > 0 &&
            listOf("gameid", "achievementsets").all { cache.containsKey(hash("prepared:$normalizedRomHash:$it")) || cache.containsKey(hash("prepared:$gameId:$it")) } &&
            listOf("unlocks", "startsession").all { cache.containsKey(hash("prepared:$gameId:$it")) }
    }

    fun invalidate() = synchronized(lock) {
        cache.clear()
        inventory.clear()
        unlocks.clear()
        loaded = true
        file.delete()
    }

    fun recordResponse(key: String, statusCode: Int, body: String) = synchronized(lock) {
        loadLocked()
        if (statusCode in 200..299 && body.length <= MAX_RESPONSE_CHARS) {
            cache[hash(key)] = CachedResponse(statusCode, body)
            persistLocked()
        }
    }

    fun recordRequest(method: String, url: String, postData: String?, responseSize: Int? = null) = synchronized(lock) {
        loadLocked()
        val safeUrl = redact(url)
        val safePost = postData?.let(::redact) ?: ""
        inventory += "method=$method url=$safeUrl post=$safePost response_size=${responseSize ?: ""}"
        if (inventory.size > MAX_INVENTORY) inventory = inventory.takeLast(MAX_INVENTORY).toMutableList()
        persistLocked()
    }

    fun recordUnlock(achievementId: Long, gameHash: String, unlockTimeMillis: Long) = synchronized(lock) {
        loadLocked()
        unlocks += "id=$achievementId hash=${hash(gameHash)} unlock_time_ms=$unlockTimeMillis mode=softcore state=pending"
        if (unlocks.size > MAX_UNLOCKS) unlocks = unlocks.takeLast(MAX_UNLOCKS).toMutableList()
        persistLocked()
    }

    fun inventorySnapshot(): List<String> = synchronized(lock) {
        loadLocked()
        inventory.toList()
    }

    fun pendingUnlockSnapshot(): List<String> = synchronized(lock) {
        loadLocked()
        unlocks.toList()
    }

    private fun loadLocked() {
        if (loaded) return
        loaded = true
        if (!file.isFile) return
        try {
            val clear = decrypt(file.readBytes()).toString(StandardCharsets.UTF_8)
            clear.lineSequence().forEach { line ->
                when {
                    line.startsWith("C|") -> {
                        val p = line.split('|', limit = 3)
                        if (p.size == 3) {
                            val encoded = p[2]
                            cache[p[1]] = CachedResponse(
                                encoded.substringBefore(':').toInt(),
                                String(Base64.decode(encoded.substringAfter(':'), Base64.NO_WRAP), StandardCharsets.UTF_8),
                            )
                        }
                    }
                    line.startsWith("I|") -> inventory += line.removePrefix("I|")
                    line.startsWith("U|") -> unlocks += line.removePrefix("U|")
                }
            }
        } catch (_: Exception) {
            cache.clear()
            inventory.clear()
            unlocks.clear()
        }
    }

    private fun persistLocked() {
        val clear = buildString {
            cache.forEach { (key, value) ->
                append("C|").append(key).append('|').append(value.statusCode).append(':')
                    .append(Base64.encodeToString(value.body.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                    .append('\n')
            }
            inventory.forEach { append("I|").append(it).append('\n') }
            unlocks.forEach { append("U|").append(it).append('\n') }
        }.toByteArray(StandardCharsets.UTF_8)
        val encrypted = encrypt(clear)
        val temp = File(file.parentFile, file.name + ".tmp")
        temp.writeBytes(encrypted)
        if (!temp.renameTo(file)) {
            file.delete()
            check(temp.renameTo(file)) { "Could not atomically persist RA offline spike data" }
        }
    }

    private fun redact(value: String): String = value
        .replace(Regex("(?i)(api_token|token|t|p|password|pass|v)=([^&\\s]+)"), "$1=<redacted>")
        .replace(Regex("(?i)(username|u)=([^&\\s]+)"), "$1=<redacted>")

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(keyAlias, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build())
        return generator.generateKey()
    }

    private fun encrypt(clear: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(clear)
    }

    private fun decrypt(encrypted: ByteArray): ByteArray {
        require(encrypted.size > GCM_IV_LENGTH)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, encrypted.copyOfRange(0, GCM_IV_LENGTH)))
        return cipher.doFinal(encrypted.copyOfRange(GCM_IV_LENGTH, encrypted.size))
    }

    companion object {
        private const val MEDIA_URL = "https://media.retroachievements.org"
        private const val GCM_IV_LENGTH = 12
        private const val MAX_RESPONSE_CHARS = 2_000_000
        private const val MAX_INVENTORY = 2_000
        private const val MAX_UNLOCKS = 500
    }
}
