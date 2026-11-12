package com.swordfish.libretrodroid

import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** HTTP and UI-event bridge for the native rcheevos client. */
@Keep
object RetroAchievementsNativeBridge {
    data class Event(
        val type: Int,
        val title: String,
        val detail: String,
        val points: Int,
        val badgeUrl: String,
        val gameHash: String = "",
        val achievementId: Long = 0L,
        val unlockTimeMillis: Long = 0L,
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()
    private val mutableEvents = MutableSharedFlow<Event>(extraBufferCapacity = 32)
    private var offlineStore: RetroAchievementsOfflineSpikeStore? = null
    val events = mutableEvents.asSharedFlow()

    /** Enables durable response replay for production offline RetroAchievements sessions. */
    @JvmStatic
    @Keep
    fun initializeOfflineCache(context: Context) {
        offlineStore = RetroAchievementsOfflineSpikeStore(context.applicationContext)
    }

    @JvmStatic
    @Keep
    fun prepareOfflineGame(context: Context, romHash: String, gameId: Int, webApiBody: String, username: String) {
        if (offlineStore == null) initializeOfflineCache(context)
        offlineStore?.prepareGame(romHash, gameId, webApiBody, username)
    }

    @JvmStatic
    @Keep
    fun isOfflineGameReady(context: Context, romHash: String, gameId: Int): Boolean {
        if (offlineStore == null) initializeOfflineCache(context)
        return offlineStore?.isPrepared(romHash, gameId) == true
    }

    @JvmStatic
    @Keep
    fun generateRetroAchievementsHash(romBytes: ByteArray, consoleId: Int): String? =
        LibretroDroid.generateRetroAchievementsHash(romBytes, consoleId)

    @JvmStatic
    @Keep
    fun invalidateOfflineCache(context: Context) {
        offlineStore?.invalidate()
        offlineStore = RetroAchievementsOfflineSpikeStore(context.applicationContext)
    }

    @JvmStatic
    @Keep
    fun request(requestId: Long, url: String, postData: String?, contentType: String?) {
        val method = if (postData == null) "GET" else "POST"
        val key = url + "\n" + (postData ?: "")
        val normalizedKey = normalizedKey(url, postData)
        val requestAction = action(url, postData)
        // rcheevos uses m for the ROM hash on gameid/achievementsets and h
        // for the hardcore flag on startsession.
        val requestHash = param(url, postData, "m") ?: param(url, postData, "h")
        val requestGameId = param(url, postData, "g")?.toIntOrNull()
        offlineStore?.recordRequest(method, url, postData)
        val builder = Request.Builder().url(url)
        if (postData != null) {
            val mediaType = (contentType ?: "application/x-www-form-urlencoded").toMediaTypeOrNull()
            builder.post(postData.toRequestBody(mediaType))
        }
        builder.header("User-Agent", "OMINOY/1.18.0 (rcheevos)")
        val call = client.newCall(builder.build())
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, error: IOException) {
                val cached = offlineStore?.cachedResponse(key)
                    ?: offlineStore?.cachedResponse(normalizedKey)
                    ?: offlineStore?.preparedResponse(requestAction, requestHash, requestGameId)
                if (cached != null) {
                    offlineStore?.recordRequest(method, url, postData, cached.body.length)
                    LibretroDroid.completeRaServerRequest(requestId, cached.statusCode, cached.body)
                } else if (requestAction == "awardachievement") {
                    // rcheevos treats this as a successful casual unlock. The app-level
                    // event handler durably queues the original unlock for later upload.
                    LibretroDroid.completeRaServerRequest(
                        requestId,
                        200,
                        offlineAwardResponse(postData),
                    )
                } else {
                    // rcheevos treats -1 as a non-retryable client error. -2 keeps
                    // achievement awards pending and lets rc_client retry later.
                    LibretroDroid.completeRaServerRequest(requestId, -2, "")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    val body = it.body?.string().orEmpty()
                    offlineStore?.recordResponse(key, it.code, body)
                    if (normalizedKey != key) offlineStore?.recordResponse(normalizedKey, it.code, body)
                    offlineStore?.recordRequest(method, url, postData, body.length)
                    LibretroDroid.completeRaServerRequest(requestId, it.code, body)
                }
            }
        })
    }

    @JvmStatic
    @Keep
    fun onNativeEvent(
        type: Int,
        title: String?,
        detail: String?,
        points: Int,
        badgeUrl: String?,
        gameHash: String?,
        achievementId: Long,
        unlockTimeMillis: Long,
    ) {
        if (type == 1 && achievementId != 0L) {
            offlineStore?.recordUnlock(achievementId, gameHash.orEmpty(), unlockTimeMillis)
        }
        mutableEvents.tryEmit(
            Event(type, title.orEmpty(), detail.orEmpty(), points, badgeUrl.orEmpty(), gameHash.orEmpty(), achievementId, unlockTimeMillis),
        )
    }

    private fun action(url: String, postData: String?): String {
        val source = listOfNotNull(url.substringAfter('?', ""), postData).joinToString("&")
        return source.split('&').firstNotNullOfOrNull { part ->
            val pieces = part.split('=', limit = 2)
            if (pieces.size == 2 && pieces[0] == "r") pieces[1].lowercase() else null
        }.orEmpty()
    }

    private fun param(url: String, postData: String?, name: String): String? =
        listOfNotNull(url.substringAfter('?', ""), postData).asSequence()
            .flatMap { it.split('&').asSequence() }
            .map { it.split('=', limit = 2) }
            .firstOrNull { it.size == 2 && it[0] == name }
            ?.get(1)

    /** Removes volatile token/validation/offset fields while retaining game and user identity. */
    private fun normalizedKey(url: String, postData: String?): String {
        val source = listOfNotNull(url.substringBefore('?'), url.substringAfter('?', ""), postData)
            .joinToString("\n")
        val action = action(url, postData)
        val stable = source.split('&', '\n')
            .filter { part ->
                val name = part.substringBefore('=', part).lowercase()
                name !in setOf("t", "v", "o", "z", "f")
            }
            .sorted()
            .joinToString("&")
        return "normalized:$action:$stable"
    }

    private fun offlineAwardResponse(postData: String?): String {
        val achievementId = postData.orEmpty().split('&').firstNotNullOfOrNull { part ->
            val pieces = part.split('=', limit = 2)
            if (pieces.size == 2 && pieces[0] == "a") pieces[1] else null
        }.orEmpty()
        return "{\"Success\":true,\"AchievementID\":${achievementId.ifBlank { "0" }},\"Error\":\"queued_offline\"}"
    }
}
