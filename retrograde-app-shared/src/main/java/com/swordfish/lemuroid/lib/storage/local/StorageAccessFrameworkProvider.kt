package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.extractEntryToFile
import com.swordfish.lemuroid.common.kotlin.isZipped
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.DataFile
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.io.InputStream
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

    override fun listBaseStorageFiles(): Observable<List<BaseStorageFile>> {
        return getExternalFolder()?.let { folder ->
            traverseDirectoryEntries(Uri.parse(folder))
        } ?: Observable.empty()
    }

    override fun getStorageFile(baseStorageFile: BaseStorageFile): StorageFile? {
        return DocumentFileParser.parseDocumentFile(context, baseStorageFile)
    }

    private fun getExternalFolder(): String? {
        val prefString = context.getString(R.string.pref_key_extenral_folder)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        return preferenceManager.getString(prefString, null)
    }

    private fun traverseDirectoryEntries(rootUri: Uri): Observable<List<BaseStorageFile>> = Observable.create { emitter ->
        try {
            val directoryDocumentIds = mutableListOf<String>()
            DocumentsContract.getTreeDocumentId(rootUri)?.let { directoryDocumentIds.add(it) }

            while (directoryDocumentIds.isNotEmpty()) {
                val currentDirectoryDocumentId = directoryDocumentIds.removeAt(0)

                val result = runCatching { listBaseStorageFiles(rootUri, currentDirectoryDocumentId) }
                if (result.isFailure) {
                    Timber.e(result.exceptionOrNull(), "Error while listing files")
                }

                val (files, directories) = result.getOrDefault(listOf<BaseStorageFile>() to listOf<String>())

                emitter.onNext(files)
                directoryDocumentIds.addAll(directories)
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }

        emitter.onComplete()
    }

    private fun listBaseStorageFiles(treeUri: Uri, rootDocumentId: String): Pair<List<BaseStorageFile>, List<String>> {
        val resultFiles = mutableListOf<BaseStorageFile>()
        val resultDirectories = mutableListOf<String>()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocumentId)

        Timber.d("Querying files in directory: $childrenUri")

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use {
            while (it.moveToNext()) {
                val documentId = it.getString(0)
                val documentName = it.getString(1)
                val documentSize = it.getLong(2)
                val mimeType = it.getString(3)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    resultDirectories.add(documentId)
                } else {
                    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                    resultFiles.add(BaseStorageFile(
                            name = documentName,
                            size = documentSize,
                            uri = documentUri,
                            path = documentUri.path
                    ))
                }
            }
        }

        return resultFiles to resultDirectories
    }

    override fun prepareDataFile(game: Game, dataFile: DataFile) = Completable.fromAction {
        val cacheFile = GameCacheUtils.getDataFileForGame(SAF_CACHE_SUBFOLDER, context, game, dataFile)
        if (cacheFile.exists()) {
            return@fromAction
        }

        val stream = context.contentResolver.openInputStream(Uri.parse(dataFile.fileUri))!!
        stream.writeToFile(cacheFile)
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val cacheFile = GameCacheUtils.getCacheFileForGame(SAF_CACHE_SUBFOLDER, context, game)
        if (cacheFile.exists()) {
            return@fromCallable cacheFile
        }

        val originalDocumentUri = Uri.parse(game.fileUri)
        val originalDocument = DocumentFile.fromSingleUri(context, originalDocumentUri)!!

        if (originalDocument.isZipped() && originalDocument.name != game.fileName) {
            val stream = ZipInputStream(context.contentResolver.openInputStream(originalDocument.uri))
            stream.extractEntryToFile(game.fileName, cacheFile)
        } else {
            val stream = context.contentResolver.openInputStream(originalDocument.uri)!!
            stream.writeToFile(cacheFile)
        }
        cacheFile
    }

    override fun getInputStream(uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    companion object {
        const val SAF_CACHE_SUBFOLDER = "storage-framework-games"
    }
}
