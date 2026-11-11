package com.swordfish.lemuroid.metadata.libretrodb

import android.net.Uri
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom
import java.text.Normalizer
import java.util.Locale

class LibretroDBArtworkProvider(private val dbManager: LibretroDBManager) {
    suspend fun resolve(metadata: GameMetadata): GameMetadata {
        if (metadata.thumbnail != null) return metadata

        val name = metadata.name ?: return metadata
        val systemId = metadata.system ?: return metadata
        val candidates = dbManager.dbInstance.gameDao().findArtworkByNormalizedName(systemId, normalize(name))
        val preferredRegion = if (hasRegion(name, "Japan")) "Japan" else "USA"

        val rom =
            candidates.firstOrNull { it.artworkName() == name.replace(INVALID_URL_CHARS, "_") }
                ?: candidates.firstOrNull { hasRegion(it.artworkName().orEmpty(), preferredRegion) }
                ?: candidates.firstOrNull { hasRegion(it.artworkName().orEmpty(), "World") }
                ?: candidates.firstOrNull()

        val artworkName = rom?.artworkName() ?: return metadata
        return metadata.copy(thumbnail = coverUrl(systemId, artworkName))
    }

    fun coverUrl(
        systemId: String,
        artworkName: String?,
    ): String? {
        if (artworkName == null) {
            return null
        }

        val system = GameSystem.findById(systemId)
        val systemName = if (system.id == SystemID.MAME2003PLUS) "MAME" else system.libretroFullName
        val fileName = Uri.encode("${artworkName.replace(INVALID_URL_CHARS, "_")}.png")
        return "$THUMBNAIL_SERVER/${Uri.encode(systemName)}/Named_Boxarts/$fileName"
    }

    private fun normalize(name: String): String {
        val normalizedName =
            Normalizer.normalize(
                stripTrailingTags(name.replace(INVALID_URL_CHARS, "_")).lowercase(Locale.ROOT),
                Normalizer.Form.NFKD,
            )

        return normalizedName
            .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter(String::isNotEmpty)
            .joinToString(" ")
    }

    private fun stripTrailingTags(name: String): String {
        var title = name
        while (TITLE_SUFFIX.containsMatchIn(title)) {
            title = title.replace(TITLE_SUFFIX, "")
        }
        return title
    }

    private fun hasRegion(name: String, region: String): Boolean =
        TAGS.findAll(name).any { match ->
            match.groupValues[1].split(',').any { it.trim() == region }
        }

    private fun LibretroRom.artworkName(): String? = artwork?.ifEmpty { name }

    private companion object {
        val TAGS = Regex("\\(([^()]*)\\)")
        const val THUMBNAIL_SERVER = "https://thumbnails.libretro.com"
        val INVALID_URL_CHARS = Regex("[&*/:`<>?\\\\|]")
        val TITLE_SUFFIX = Regex("\\s*[\\[(][^\\[\\]()]*[\\])]\\s*$")
    }
}
