package com.swordfish.lemuroid.lib.scraper

data class AlphaLemuroidMetadata(
    val id: Long? = null,
    val name: String? = null,
    val summary: String? = null,
    val storyline: String? = null,
    val releaseDate: String? = null,
    val rating: Float? = null,
    val ratingCount: Int? = null,
    val genres: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val gameModes: List<String> = emptyList(),
    val developers: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val coverUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val artworks: List<String> = emptyList(),
    val videoYoutubeUrls: List<String> = emptyList(),
)
