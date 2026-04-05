package com.swordfish.lemuroid.lib.library

import android.content.Context
import android.net.Uri
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

    fun getCoversDirectory(): File =
        File(appContext.getExternalFilesDir(null), "custom-covers").apply {
            mkdirs()
        }

    private fun coverFileForGame(gameId: Int): File =
        File(getCoversDirectory(), "cover_$gameId.png")

    suspend fun importCover(gameId: Int, sourceUri: Uri): String =
        withContext(Dispatchers.IO) {
            val destFile = coverFileForGame(gameId)
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Cannot open input stream for $sourceUri")
            Uri.fromFile(destFile).toString()
        }

    fun deleteCover(gameId: Int) {
        val file = coverFileForGame(gameId)
        if (file.exists()) {
            val deleted = file.delete()
            Timber.d("Deleted custom cover for game $gameId: $deleted")
        }
    }

    fun deleteCoversForGames(games: List<Game>) {
        games.forEach { game ->
            if (game.customCoverUri != null) {
                deleteCover(game.id)
            }
        }
    }
}
