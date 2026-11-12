package come.asg.ominoy.lib.library.covers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/** Manages durable, processed custom cover artwork in app-private storage. */
class CustomCoverManager(private val appContext: Context) {
    fun getCoversDirectory(): File {
        val parent = appContext.getExternalFilesDir(null) ?: appContext.filesDir
        return File(parent, "custom-covers").apply {
            check(mkdirs() || isDirectory) { "Cannot create custom covers directory: $absolutePath" }
        }
    }

    private fun coverFileForGame(gameId: Int): File = File(getCoversDirectory(), "cover_$gameId.jpg")

    /** Imports, center-crops, and scales artwork, returning a durable local URI. */
    suspend fun importCover(gameId: Int, sourceUri: Uri): String =
        withContext(Dispatchers.IO) {
            val source = decodeSampled(sourceUri)
                ?: throw IllegalStateException("Cannot decode image for custom cover: $sourceUri")
            val destination = coverFileForGame(gameId)
            val directory = destination.parentFile ?: throw IllegalStateException("Missing cover directory")
            val temporary = File(directory, "cover_$gameId.${System.nanoTime()}.tmp")
            var processed: Bitmap? = null

            try {
                processed = process(source)
                FileOutputStream(temporary).use { output ->
                    check(processed.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                        "Cannot encode custom cover for game $gameId"
                    }
                    output.fd.sync()
                }

                if (destination.exists()) {
                    val backup = File(directory, "cover_$gameId.backup_${System.nanoTime()}.jpg")
                    check(destination.renameTo(backup)) { "Cannot preserve existing artwork for game $gameId" }
                    if (!temporary.renameTo(destination)) {
                        backup.renameTo(destination)
                        error("Cannot install new artwork for game $gameId")
                    }
                    backup.delete()
                } else {
                    check(temporary.renameTo(destination)) { "Cannot install artwork for game $gameId" }
                }
                deleteOtherCoversForGameId(gameId, destination)
                Uri.fromFile(destination).toString()
            } finally {
                temporary.delete()
                if (processed !== source) processed?.recycle()
                source.recycle()
            }
        }

    fun deleteCover(gameId: Int) = deleteCoversForGameId(gameId)

    fun deleteCoversForGames(games: List<Game>) {
        games.forEach { game ->
            if (game.customCoverUri != null) deleteCoversForGameId(game.id)
        }
    }

    private fun decodeSampled(uri: Uri): Bitmap? {
        val resolver = appContext.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = resolver.openInputStream(uri) ?: return null
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_DECODE_EDGE) sampleSize *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun process(source: Bitmap): Bitmap {
        val side = minOf(source.width, source.height)
        val square = Bitmap.createBitmap(source, (source.width - side) / 2, (source.height - side) / 2, side, side)
        if (side <= MAX_DIMENSION) return square
        return Bitmap.createScaledBitmap(square, MAX_DIMENSION, MAX_DIMENSION, true).also {
            if (it !== square) square.recycle()
        }
    }

    private fun deleteCoversForGameId(gameId: Int) {
        deleteOtherCoversForGameId(gameId, keep = null)
    }

    private fun deleteOtherCoversForGameId(gameId: Int, keep: File?) {
        val prefix = "cover_$gameId."
        getCoversDirectory().listFiles()
            ?.filter { it != keep && it.name.startsWith(prefix) }
            ?.forEach { file -> Timber.d("Deleted custom cover ${file.name}: ${file.delete()}") }
    }

    private companion object {
        const val MAX_DECODE_EDGE = 2048
        const val MAX_DIMENSION = 512
        const val JPEG_QUALITY = 90
    }
}
