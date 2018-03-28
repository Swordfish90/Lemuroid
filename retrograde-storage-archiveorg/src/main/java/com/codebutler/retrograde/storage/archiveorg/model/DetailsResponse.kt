package com.codebutler.retrograde.storage.archiveorg.model

data class DetailsResponse(
    val server: String,
    val dir: String,
    val metadata: Metadata,
    val files: Map<String, File>,
    val misc: Misc
) {
    data class Metadata(val title: List<String>, val creator: List<String>?)
    data class File(val format: String)
    data class Misc(val image: String)
}
