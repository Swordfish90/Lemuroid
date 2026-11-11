package com.swordfish.lemuroid.lib.library.covers

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Manages custom cover artwork files stored in the app's private external storage.
 *
 * Images are copied into [getCoversDirectory] with a unique filename prefixed by the
 * game's database id so that:
 *   - The URI never expires (it's a local file we own).
 *   - Cleanup is trivial: delete the file when the game or the custom artwork is removed.
 */
class CustomCoverManager(private val appContext: Context) {

    /** Returns (and creates if necessary) the directory for custom covers. */
    private fun getCoversDirectory(): File =
        File(appContext.getExternalFilesDir(null), "custom-covers").apply {
            mkdirs()
        }

    /**
     * Resolves a file extension from the source URI's MIME type.
     * Falls back to "png" if the type can't be determined.
     */
    private fun extensionForUri(sourceUri: Uri): String {
        val mimeType = appContext.contentResolver.getType(sourceUri)
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return ext ?: "png"
    }

    /** Build the cover file path for a given game id and extension. */
    private fun coverFileForGame(gameId: Int, extension: String): File =
        File(getCoversDirectory(), "cover_${gameId}.${UUID.randomUUID()}.$extension")

    /** Copies to a new file so a failed edit preserves the previous cover. */
    suspend fun importCover(gameId: Int, sourceUri: Uri): String =
        withContext(Dispatchers.IO) {
            val destFile = coverFileForGame(gameId, extensionForUri(sourceUri))
            try {
                appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                } ?: error("Cannot open image")
                Uri.fromFile(destFile).toString()
            } catch (exception: Exception) {
                destFile.delete()
                throw exception
            }
        }

    /** Deletes only files owned by this manager. */
    fun deleteCoverUri(uri: String?) {
        if (uri == null || Uri.parse(uri).scheme != "file") return
        val file = Uri.parse(uri).path?.let(::File) ?: return
        if (file.parentFile?.canonicalFile == getCoversDirectory().canonicalFile) file.delete()
    }

    /** Deletes the stored covers when games are removed from the library. */
    fun deleteCoversForGames(games: List<Game>) {
        games.forEach { deleteCoverUri(it.coverFrontUrl) }
    }
}
