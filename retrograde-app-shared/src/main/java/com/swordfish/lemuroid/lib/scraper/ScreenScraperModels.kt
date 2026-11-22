package com.swordfish.lemuroid.lib.scraper

import kotlinx.serialization.Serializable

@Serializable
data class ScreenScraperResponse(
    val response: ScreenScraperContainer? = null,
)

@Serializable
data class ScreenScraperContainer(
    val jeu: ScreenScraperJeu? = null,
)

@Serializable
data class ScreenScraperJeu(
    val id: String? = null,
    val romid: String? = null,
    val noms: List<ScreenScraperRegionTextEntry>? = null,
    val synopsis: List<ScreenScraperSynopsis>? = null,
    val developpeur: ScreenScraperTextEntry? = null,
    val editeur: ScreenScraperTextEntry? = null,
    val genres: List<ScreenScraperGenre>? = null,
    val dates: List<ScreenScraperRegionTextEntry>? = null,
    val note: ScreenScraperTextEntry? = null,
    val joueurs: ScreenScraperTextEntry? = null,
    val medias: List<ScreenScraperMedia>? = null,
)

@Serializable
data class ScreenScraperSynopsis(
    val langue: String? = null,
    val text: String? = null,
)

@Serializable
data class ScreenScraperGenre(
    val id: String? = null,
    val nom: String? = null,
    val noms: List<ScreenScraperSynopsis>? = null,
)

@Serializable
data class ScreenScraperTextEntry(
    val id: String? = null,
    val text: String? = null,
)

@Serializable
data class ScreenScraperRegionTextEntry(
    val region: String? = null,
    val text: String? = null,
)

@Serializable
data class ScreenScraperMedia(
    val type: String? = null,
    val parent: String? = null,
    val url: String? = null,
    val region: String? = null,
    val format: String? = null,
)

data class ScrapedGameData(
    val title: String? = null,
    val synopsis: String? = null,
    val genres: String? = null,
    val publisher: String? = null,
    val developer: String? = null,
    val releaseDate: String? = null,
    val players: String? = null,
    val rating: Float? = null,
    val bannerUrl: String? = null,
    val logoUrl: String? = null,
    val boxArtFrontUrl: String? = null,
    val boxArtBackUrl: String? = null,
    val screenshotUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val scraperSource: String = "ScreenScraper",
)
