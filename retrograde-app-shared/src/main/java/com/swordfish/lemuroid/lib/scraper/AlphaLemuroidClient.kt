package com.swordfish.lemuroid.lib.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AlphaLemuroidClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "AlphaLemuroid")
                .build()
            chain.proceed(request)
        }
        .dispatcher(
            Dispatcher().apply {
                maxRequests = 32
                maxRequestsPerHost = 15
            },
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
) {
    companion object {
        private const val API_URL = "https://api.alphalemuroid.workers.dev/scrape"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    data class UnifiedScrapeResult(
        val igdb: AlphaLemuroidMetadata?,
        val screenScraperJeu: JSONObject?,
    )

    suspend fun scrapeUnified(
        crc32: String? = null,
        title: String? = null,
        platformId: Int? = null,
        romName: String? = null,
        systemEid: Int? = null,
        romSize: Long? = null,
        userId: String? = null,
        userPassword: String? = null,
        source: String = "mixed",
    ): UnifiedScrapeResult? = withContext(Dispatchers.IO) {
        val cleanCrc = crc32?.trim()?.uppercase()?.takeIf { it.isNotBlank() && it != "0" }
        val cleanTitle = cleanGameTitle(title)
        val hasSsParams = systemEid != null && !romName.isNullOrBlank()

        if (cleanCrc == null && cleanTitle.isBlank() && !hasSsParams) return@withContext null

        val jsonBody = JSONObject().apply {
            put("source", source.lowercase())
            if (!cleanCrc.isNullOrBlank()) {
                put("crc32", cleanCrc)
            }
            if (cleanTitle.isNotBlank()) {
                put("title", cleanTitle)
            }
            if (platformId != null && platformId > 0) {
                put("platformId", platformId)
            }
            if (hasSsParams) {
                put("romName", romName)
                put("systemEid", systemEid)
                if (romSize != null && romSize > 0L) {
                    put("romSize", romSize)
                }
            }
        }

        val requestBuilder = Request.Builder()
            .url(API_URL)
            .header("Content-Type", "application/json")
            .header("User-Agent", "AlphaLemuroid")
            .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))

        if (!userId.isNullOrBlank() && !userPassword.isNullOrBlank()) {
            requestBuilder.header("X-SS-User", userId)
            requestBuilder.header("X-SS-Password", userPassword)
        }

        val jsonString = runCatching {
            okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body?.string()
            }
        }.getOrNull() ?: return@withContext null

        runCatching {
            val root = JSONObject(jsonString)
            val igdb = if (root.has("name") || root.has("id")) parseResponse(jsonString) else null
            val ssJeu = root.optJSONObject("screenscraper")
                ?.optJSONObject("response")
                ?.optJSONObject("jeu")
            UnifiedScrapeResult(igdb = igdb, screenScraperJeu = ssJeu)
        }.getOrNull()
    }

    suspend fun scrapeMetadata(
        crc32: String? = null,
        title: String? = null,
        platformId: Int? = null,
    ): AlphaLemuroidMetadata? = withContext(Dispatchers.IO) {
        val cleanCrc = crc32?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
        val cleanTitle = cleanGameTitle(title)

        if (cleanCrc == null && cleanTitle.isBlank()) return@withContext null

        val jsonBody = JSONObject().apply {
            if (!cleanCrc.isNullOrBlank()) {
                put("crc32", cleanCrc)
            }
            if (cleanTitle.isNotBlank()) {
                put("title", cleanTitle)
            }
            if (platformId != null && platformId > 0) {
                put("platformId", platformId)
            }
        }

        val request = Request.Builder()
            .url(API_URL)
            .header("Content-Type", "application/json")
            .header("User-Agent", "AlphaLemuroid")
            .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val jsonString = runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body?.string()
            }
        }.getOrNull() ?: return@withContext null

        parseResponse(jsonString)
    }

    private fun parseResponse(jsonString: String): AlphaLemuroidMetadata? {
        return runCatching {
            val root = JSONObject(jsonString)
            if (root.has("error")) return null

            val name = root.optString("name").takeIf { it.isNotBlank() }
            val summary = root.optString("summary").takeIf { it.isNotBlank() }
            val storyline = root.optString("storyline").takeIf { it.isNotBlank() }
            val rawRating = root.optDouble("rating", -1.0).takeIf { it >= 0 }
            val rating = rawRating?.let { (it / 20.0).toFloat() }
            val ratingCount = root.optInt("total_rating_count", -1).takeIf { it > 0 }
                ?: root.optInt("rating_count", -1).takeIf { it > 0 }

            val genres = parseStringList(root, "genres")
            val themes = parseStringList(root, "themes")
            val gameModes = parseStringList(root, "game_modes")
            val developers = parseStringList(root, "developers")
            val publishers = parseStringList(root, "publishers")

            val mediaObj = root.optJSONObject("media")
            val coverObj = mediaObj?.optJSONObject("cover")
            val coverUrl = coverObj?.optString("original")?.takeIf { it.isNotBlank() }
                ?: coverObj?.optString("hd")?.takeIf { it.isNotBlank() }

            val screenshots = mutableListOf<String>()
            val ssArray = mediaObj?.optJSONArray("screenshots")
            if (ssArray != null) {
                for (i in 0 until ssArray.length()) {
                    val sObj = ssArray.optJSONObject(i) ?: continue
                    val u = sObj.optString("full_hd").takeIf { it.isNotBlank() }
                        ?: sObj.optString("hd").takeIf { it.isNotBlank() }
                    if (u != null) {
                        screenshots.add(u.replace(".jpg", ".webp"))
                    }
                }
            }

            val artworks = mutableListOf<String>()
            val artArray = mediaObj?.optJSONArray("artworks")
            if (artArray != null) {
                for (i in 0 until artArray.length()) {
                    val aObj = artArray.optJSONObject(i) ?: continue
                    val u = aObj.optString("full_hd").takeIf { it.isNotBlank() }
                        ?: aObj.optString("hd").takeIf { it.isNotBlank() }
                    if (u != null) {
                        artworks.add(u.replace(".jpg", ".webp"))
                    }
                }
            }

            val videoYoutubeUrls = mutableListOf<String>()
            val vidArray = mediaObj?.optJSONArray("videos")
            if (vidArray != null) {
                for (i in 0 until vidArray.length()) {
                    val vObj = vidArray.optJSONObject(i) ?: continue
                    val u = vObj.optString("youtube_url").takeIf { it.isNotBlank() }
                    if (u != null) {
                        videoYoutubeUrls.add(u)
                    }
                }
            }

            val releaseYear = root.optLong("first_release_date", -1L).takeIf { it > 0 }?.let { epochSec ->
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = epochSec * 1000L
                calendar.get(java.util.Calendar.YEAR).toString()
            }

            AlphaLemuroidMetadata(
                id = root.optLong("id", -1L).takeIf { it > 0 },
                name = name,
                summary = summary,
                storyline = storyline,
                releaseDate = releaseYear,
                rating = rating,
                ratingCount = ratingCount,
                genres = genres,
                themes = themes,
                gameModes = gameModes,
                developers = developers,
                publishers = publishers,
                coverUrl = coverUrl?.replace(".jpg", ".webp"),
                screenshots = screenshots,
                artworks = artworks,
                videoYoutubeUrls = videoYoutubeUrls,
            )
        }.getOrNull()
    }

    private fun parseStringList(obj: JSONObject, key: String): List<String> {
        val array = obj.optJSONArray(key) ?: return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val item = array.optString(i)
            if (item.isNotBlank()) {
                list.add(item)
            }
        }
        return list.distinct()
    }

    fun cleanGameTitle(rawTitle: String?): String {
        if (rawTitle.isNullOrBlank()) return ""
        return rawTitle
            .replace(Regex("\\s*\\([^)]*\\)"), "")
            .replace(Regex("\\s*\\[[^]]*\\]"), "")
            .replace(Regex("[_\\-]+"), " ")
            .trim()
    }
}
