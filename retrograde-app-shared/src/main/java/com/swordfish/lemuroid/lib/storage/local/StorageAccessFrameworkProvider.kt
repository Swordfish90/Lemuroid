package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.extractEntryToFile
import com.swordfish.lemuroid.common.kotlin.isZipped
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

class StorageAccessFrameworkProvider(
    private val context: Context,
    override val metadataProvider: GameMetadataProvider
) : StorageProvider {

    override val id: String = "access_framework"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("content")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listFiles(): Observable<StorageFile> {
        return getExternalFolder()?.let { folder ->
            traverseDirectoryEntries(Uri.parse(folder)).map { DocumentFileParser.parseDocumentFile(context, it) }
        } ?: Observable.empty()
    }

    private fun getExternalFolder(): String? {
        val prefString = context.getString(R.string.pref_key_extenral_folder)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        return preferenceManager.getString(prefString, null)
    }

    private fun traverseDirectoryEntries(rootUri: Uri): Observable<DocumentFile> = Observable.create { emitter ->
        try {
            var currentNode = DocumentFile.fromTreeUri(context.applicationContext, rootUri)

            // Keep track of our directory hierarchy
            val dirNodes = mutableListOf<DocumentFile>()
            currentNode?.let { dirNodes.add(it) }

            while (dirNodes.isNotEmpty()) {
                currentNode = dirNodes.removeAt(0)

                Timber.d("Detected node uri: $currentNode")

                // We see on the Google Play consoles some security exceptions thrown randomly in this method.
                // Let's try to make it as robust as possible.
                val result = runCatching { currentNode.listFiles() }
                val files = result.getOrElse { arrayOf() }

                for (file in files) {
                    runCatching {
                        if (file.isDirectory) {
                            dirNodes.add(file)
                        } else {
                            emitter.onNext(file)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }

        emitter.onComplete()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val cacheFile = GameCacheUtils.getCacheFileForGame(SAF_CACHE_SUBFOLDER, context, game)
        if (cacheFile.exists()) {
            return@fromCallable cacheFile
        }

        val originalDocument = DocumentFile.fromSingleUri(context, game.fileUri)!!

        if (originalDocument.isZipped() && originalDocument.name != game.fileName) {
            val stream = ZipInputStream(context.contentResolver.openInputStream(originalDocument.uri))
            stream.extractEntryToFile(game.fileName, cacheFile)
        } else {
            val stream = context.contentResolver.openInputStream(game.fileUri)!!
            stream.writeToFile(cacheFile)
        }
        cacheFile
    }

    companion object {
        const val SAF_CACHE_SUBFOLDER = "storage-framework-games"
    }
}
