package com.swordfish.lemuroid.lib.library

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Manages custom cover artwork files stored in the app's private external storage.
 *
 * Images are copied into [getCoversDirectory] with a deterministic filename based on the
 * game's database id so that:
 *   - The URI never expires (it's a local file we own).
 *   - Cleanup is trivial: delete the file when the game or the custom artwork is removed.
 */
class CustomCoverManager(private val appContext: Context) {

    /** Returns (and creates if necessary) the directory for custom covers. */
    fun getCoversDirectory(): File =
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
        File(getCoversDirectory(), "cover_$gameId.$extension")

    /**
     * Copies the image at [sourceUri] into private storage and returns the local
     * file:// URI string that should be persisted in [Game.customCoverUri].
     *
     * Any previously stored cover for this game is deleted first so that
     * switching from e.g. a .jpg to a .webp doesn't leave an orphan.
     */
    suspend fun importCover(gameId: Int, sourceUri: Uri): String =
        withContext(Dispatchers.IO) {
            // Remove any existing cover regardless of its extension
            deleteCoversForGameId(gameId)

            val ext = extensionForUri(sourceUri)
            val destFile = coverFileForGame(gameId, ext)
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Cannot open input stream for $sourceUri")
            Uri.fromFile(destFile).toString()
        }

    /** Deletes the locally-stored cover for [gameId], regardless of extension. */
    fun deleteCover(gameId: Int) {
        deleteCoversForGameId(gameId)
    }

    /** Batch-deletes covers for a list of games. Called during library cleanup. */
    fun deleteCoversForGames(games: List<Game>) {
        games.forEach { game ->
            if (game.customCoverUri != null) {
                deleteCoversForGameId(game.id)
            }
        }
    }

    /**
     * Deletes any file matching cover_<gameId>.* in the covers directory.
     * This ensures cleanup works regardless of which image format was used.
     */
    private fun deleteCoversForGameId(gameId: Int) {
        val prefix = "cover_${gameId}."
        getCoversDirectory().listFiles()
            ?.filter { it.name.startsWith(prefix) }
            ?.forEach { file ->
                val deleted = file.delete()
                Timber.d("Deleted custom cover ${file.name}: $deleted")
            }
    }
}
