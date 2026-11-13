package come.asg.ominoy.lib.storage.local

import android.content.Context
import come.asg.ominoy.common.kotlin.safeBasename
import come.asg.ominoy.lib.library.db.entity.DataFile
import come.asg.ominoy.lib.library.db.entity.Game
import java.io.File
import java.security.MessageDigest

object GameCacheUtils {
    fun getDataFileForGame(
        folderName: String,
        context: Context,
        game: Game,
        dataFile: DataFile,
        sourceMetadata: String = "",
    ): File {
        val gamesCacheDir = getCacheDirForGame(folderName, game, context, sourceMetadata)
        return File(gamesCacheDir, dataFile.fileName.safeBasename())
    }

    fun getCacheFileForGame(
        folderName: String,
        context: Context,
        game: Game,
        sourceMetadata: String = "",
    ): File {
        val gamesCacheDir = getCacheDirForGame(folderName, game, context, sourceMetadata)
        return File(gamesCacheDir, game.fileName.safeBasename())
    }

    private fun getCacheDirForGame(
        folderName: String,
        game: Game,
        context: Context,
        sourceMetadata: String,
    ): File {
        val sourceKey = sha256("${game.fileUri}\u0000${game.fileName}\u0000${game.systemId}\u0000$sourceMetadata")
        val gamesCachePath = buildPath(folderName.safeBasename(), game.systemId.safeBasename(), sourceKey)
        val gamesCacheDir = File(context.cacheDir, gamesCachePath)
        gamesCacheDir.mkdirs()
        return gamesCacheDir
    }

    private fun buildPath(vararg chunks: String): String = chunks.joinToString(separator = File.separator)

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
