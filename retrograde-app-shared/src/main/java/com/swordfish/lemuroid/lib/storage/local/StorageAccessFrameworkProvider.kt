package com.swordfish.lemuroid.lib.storage.local

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
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
            val something = DocumentFile.fromTreeUri(context, rootUri)

            val dirNodes = mutableListOf<Uri>()
            dirNodes.add(something!!.uri)

            while (dirNodes.isNotEmpty()) {
                val currentUri = dirNodes.removeAt(0)

                Timber.d("Detected node uri: $currentUri")

                val (files, folders) = listBaseStorageFiles(currentUri)

                emitter.onNext(files)
                dirNodes.addAll(folders)
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }

        emitter.onComplete()
    }

    // TODO FILIPPO... Make sure this works exactly like DocumentFile.listFiles() to avoid regressions
    private fun listBaseStorageFiles(treeUri: Uri): Pair<List<BaseStorageFile>, List<Uri>> {
        val resolver: ContentResolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getDocumentId(treeUri))

        val resultFiles = mutableListOf<BaseStorageFile>()
        val resultFolders = mutableListOf<Uri>()

        var c: Cursor? = null
        try {
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )
            c = resolver.query(childrenUri, projection, null, null, null)
            while (c!!.moveToNext()) {
                val documentId = c.getString(0)
                val documentName = c.getString(1)
                val documentSize = c.getLong(2)
                val mimeType = c.getString(3)

                val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    resultFolders.add(documentUri)
                } else {
                    resultFiles.add(BaseStorageFile(
                            name = documentName,
                            size = documentSize,
                            uri = documentUri,
                            path = documentUri.path
                    ))
                }
            }
        } catch (e: java.lang.Exception) {
            Timber.e("Failed content resolver query")
        } finally {
            c?.close()
        }

        return resultFiles to resultFolders
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
