package com.swordfish.lemuroid.lib.scraper

import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class ScreenScraperClient {
    companion object {
        private const val MEDIA_PROXY_URL = "https://api.alphalemuroid.workers.dev/screenscraper/media"
    }

    fun buildMediaUrl(
        jeuid: String,
        systemEid: Int,
        media: String,
    ): String {
        return "$MEDIA_PROXY_URL?systemeid=$systemEid&jeuid=${Uri.encode(jeuid)}&media=${Uri.encode(media)}"
    }

    fun parseJeuJsonObject(
        jeu: JSONObject,
        systemEid: Int,
        boxArtFormat: String = "2D",
        mediaSources: Set<String> = emptySet(),
    ): ScrapedGameData {
        val preferredRegions = listOf("us", "wor", "ss", "eu", "jp")

    
        val nomsArray = jeu.optJSONArray("noms")
        var title: String? = null
        if (nomsArray != null) {
            for (region in preferredRegions) {
                for (i in 0 until nomsArray.length()) {
                    val obj = nomsArray.optJSONObject(i) ?: continue
                    val r = obj.optString("region").lowercase(Locale.ROOT)
                    val t = obj.optString("text")
                    if (r == region && t.isNotBlank()) {
                        title = t
                        break
                    }
                }
                if (title != null) break
            }
            if (title == null && nomsArray.length() > 0) {
                title = nomsArray.optJSONObject(0)?.optString("text")?.takeIf { it.isNotBlank() }
            }
        }
        if (title.isNullOrBlank()) {
            title = jeu.optString("nom").takeIf { it.isNotBlank() }
        }

  
        val synArray = jeu.optJSONArray("synopsis")
        var synopsis: String? = null
        if (synArray != null) {
            for (i in 0 until synArray.length()) {
                val obj = synArray.optJSONObject(i) ?: continue
                val l = obj.optString("langue").lowercase(Locale.ROOT)
                val t = obj.optString("text")
                if (l == "en" && t.isNotBlank()) {
                    synopsis = t
                    break
                }
            }
            if (synopsis == null && synArray.length() > 0) {
                synopsis = synArray.optJSONObject(0)?.optString("text")?.takeIf { it.isNotBlank() }
            }
        }
        if (synopsis.isNullOrBlank()) {
            synopsis = jeu.optString("synopsis").takeIf { it.isNotBlank() }
        }

        val developer = jeu.optJSONObject("developpeur")?.optString("text")?.takeIf { it.isNotBlank() }
            ?: jeu.optString("developpeur").takeIf { it.isNotBlank() }
        val publisher = jeu.optJSONObject("editeur")?.optString("text")?.takeIf { it.isNotBlank() }
            ?: jeu.optString("editeur").takeIf { it.isNotBlank() }

        val datesArray = jeu.optJSONArray("dates")
        var releaseDate: String? = null
        if (datesArray != null) {
            for (region in preferredRegions) {
                for (i in 0 until datesArray.length()) {
                    val obj = datesArray.optJSONObject(i) ?: continue
                    val r = obj.optString("region").lowercase(Locale.ROOT)
                    val t = obj.optString("text")
                    if (r == region && t.isNotBlank()) {
                        releaseDate = t
                        break
                    }
                }
                if (releaseDate != null) break
            }
            if (releaseDate == null && datesArray.length() > 0) {
                releaseDate = datesArray.optJSONObject(0)?.optString("text")?.takeIf { it.isNotBlank() }
            }
        }
        if (releaseDate.isNullOrBlank()) {
            releaseDate = jeu.optString("date").takeIf { it.isNotBlank() }
        }

        val players = jeu.optJSONObject("joueurs")?.optString("text")?.takeIf { it.isNotBlank() }
            ?: jeu.optString("joueurs").takeIf { it.isNotBlank() }

        val noteObj = jeu.optJSONObject("note")
        val noteStr = noteObj?.optString("text")?.takeIf { it.isNotBlank() }
            ?: noteObj?.optString("id")?.takeIf { it.isNotBlank() }
            ?: jeu.optString("note").takeIf { it.isNotBlank() && it != "null" }
            ?: jeu.optString("notefouineur").takeIf { it.isNotBlank() && it != "null" }
        val rawNote = noteStr?.toFloatOrNull() ?: if (jeu.has("note")) {
            jeu.optDouble("note", -1.0).takeIf { it >= 0 }?.toFloat()
        } else null
        val rating = rawNote?.let {
            if (it > 5.0f) it / 4.0f else it
        } ?: -1.0f

        val genresArray = jeu.optJSONArray("genres")
        val genresList = mutableListOf<String>()
        if (genresArray != null) {
            for (i in 0 until genresArray.length()) {
                val gObj = genresArray.optJSONObject(i) ?: continue
                val noms = gObj.optJSONArray("noms")
                var genreText: String? = null
                if (noms != null) {
                    for (j in 0 until noms.length()) {
                        val nObj = noms.optJSONObject(j) ?: continue
                        if (nObj.optString("langue").lowercase(Locale.ROOT) == "en") {
                            genreText = nObj.optString("text").takeIf { it.isNotBlank() }
                            if (genreText != null) break
                        }
                    }
                }
                if (genreText == null) {
                    genreText = gObj.optString("nom").takeIf { it.isNotBlank() }
                }
                if (!genreText.isNullOrBlank()) {
                    genresList.add(genreText)
                }
            }
        }
        val genres = genresList.distinct().joinToString(", ").takeIf { it.isNotBlank() }

        val mediasArray = jeu.optJSONArray("medias")
        val medias = mutableListOf<ScreenScraperMedia>()
        if (mediasArray != null) {
            for (i in 0 until mediasArray.length()) {
                val m = mediasArray.optJSONObject(i) ?: continue
                val url = m.optString("url")
                if (url.isNotBlank()) {
                    medias.add(
                        ScreenScraperMedia(
                            type = m.optString("type").takeIf { it.isNotBlank() },
                            parent = m.optString("parent").takeIf { it.isNotBlank() },
                            url = url,
                            region = m.optString("region").takeIf { it.isNotBlank() },
                            format = m.optString("format").takeIf { it.isNotBlank() },
                        )
                    )
                }
            }
        }

        val jeuid = jeu.optString("id").takeIf { it.isNotBlank() }

        val bannerUrl = if (mediaSources.contains("banner")) {
            medias.firstOrNull { it.type.equals("fanart", ignoreCase = true) }?.url
                ?: medias.firstOrNull { it.type.equals("steamgrid", ignoreCase = true) }?.url
                ?: medias.firstOrNull { it.type.equals("background", ignoreCase = true) }?.url
                ?: jeuid?.let { buildMediaUrl(it, systemEid, "fanart") }
        } else null

        val logoUrl = preferredRegions.firstNotNullOfOrNull { region ->
            medias.firstOrNull {
                it.type.equals("wheel", ignoreCase = true) &&
                    it.region.equals(region, ignoreCase = true)
            }?.url
        } ?: medias.firstOrNull { it.type.equals("wheel", ignoreCase = true) }?.url
            ?: medias.firstOrNull { it.type?.contains("wheel", ignoreCase = true) == true }?.url
            ?: jeuid?.let { buildMediaUrl(it, systemEid, "wheel(wor)") }

        val primaryBoxType = if (boxArtFormat.equals("3D", ignoreCase = true)) "box-3D" else "box-2D"
        val secondaryBoxType = if (primaryBoxType == "box-3D") "box-2D" else "box-3D"

        val boxArtFrontUrl = preferredRegions.firstNotNullOfOrNull { region ->
            medias.firstOrNull {
                it.type.equals(primaryBoxType, ignoreCase = true) &&
                    it.region.equals(region, ignoreCase = true)
            }?.url
        } ?: medias.firstOrNull { it.type.equals(primaryBoxType, ignoreCase = true) }?.url
            ?: preferredRegions.firstNotNullOfOrNull { region ->
                medias.firstOrNull {
                    it.type.equals(secondaryBoxType, ignoreCase = true) &&
                        it.region.equals(region, ignoreCase = true)
                }?.url
            } ?: medias.firstOrNull { it.type.equals(secondaryBoxType, ignoreCase = true) }?.url
            ?: medias.firstOrNull { it.type?.contains("box", ignoreCase = true) == true }?.url
            ?: jeuid?.let { buildMediaUrl(it, systemEid, "$primaryBoxType(us)") }

        val boxArtBackUrl = preferredRegions.firstNotNullOfOrNull { region ->
            medias.firstOrNull {
                it.type?.contains("back", ignoreCase = true) == true &&
                    it.region.equals(region, ignoreCase = true)
            }?.url
        } ?: medias.firstOrNull { it.type?.contains("back", ignoreCase = true) == true }?.url

        val videoUrl = if (mediaSources.contains("video")) {
            medias.firstOrNull { it.type.equals("video-normalized", ignoreCase = true) }?.url
                ?: medias.firstOrNull { it.type.equals("video", ignoreCase = true) }?.url
                ?: jeuid?.let { buildMediaUrl(it, systemEid, "video-normalized") }
        } else null

        val fromMedias = medias
            .filter {
                val t = it.type?.lowercase(Locale.ROOT) ?: return@filter false
                mediaSources.any { source ->
                    when (source) {
                        "ss" -> t == "ss" || t.startsWith("ss(")
                        "sstitle" -> t == "sstitle" || t.startsWith("sstitle(")
                        "fanart" -> t == "fanart" || t.startsWith("fanart(")
                        else -> false
                    }
                }
            }
            .mapNotNull { it.url }

        val screenshotUrls = if (fromMedias.isNotEmpty()) {
            fromMedias.distinct().take(25)
        } else if (jeuid != null) {
            val fallbackList = mutableListOf<String>()
            if (mediaSources.contains("ss")) {
                fallbackList.add(buildMediaUrl(jeuid, systemEid, "ss(wor)"))
            }
            if (mediaSources.contains("sstitle")) {
                fallbackList.add(buildMediaUrl(jeuid, systemEid, "sstitle(wor)"))
            }
            if (mediaSources.contains("fanart")) {
                fallbackList.add(buildMediaUrl(jeuid, systemEid, "fanart"))
            }
            fallbackList
        } else {
            emptyList()
        }

        return ScrapedGameData(
            title = title,
            synopsis = synopsis,
            genres = genres,
            publisher = publisher,
            developer = developer,
            releaseDate = releaseDate,
            players = players,
            rating = rating,
            bannerUrl = bannerUrl,
            logoUrl = logoUrl,
            boxArtFrontUrl = boxArtFrontUrl,
            boxArtBackUrl = boxArtBackUrl,
            screenshotUrls = screenshotUrls,
            videoUrl = videoUrl,
            scraperSource = "ScreenScraper",
        )
    }

    fun serializeScreenshots(screenshotUrls: List<String>): String {
        val array = JSONArray()
        screenshotUrls.forEach { array.put(it) }
        return array.toString()
    }

    fun deserializeScreenshots(jsonString: String?): List<String> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val str = array.optString(i)
                if (str.isNotBlank()) {
                    list.add(str)
                }
            }
            list
        }.getOrDefault(emptyList())
    }
}
