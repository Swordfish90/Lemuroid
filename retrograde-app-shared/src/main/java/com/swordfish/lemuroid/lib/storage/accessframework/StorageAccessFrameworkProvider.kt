package com.swordfish.lemuroid.lib.storage.accessframework

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.toStringCRC32
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.ISOScanner
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import com.swordfish.lemuroid.lib.storage.local.LocalStorageUtils
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
            traverseDirectoryEntries(Uri.parse(folder)).map { handleFileUri(it) }
        } ?: Observable.empty()
    }

    private fun getExternalFolder(): String? {
        val prefString = context.getString(R.string.pref_key_extenral_folder)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        return preferenceManager.getString(prefString, null)
    }

    private fun handleFileUri(fileUri: FileUri): StorageFile {
        return if (isZipped(fileUri.mime) && isSingleArchive(fileUri.uri)) {
            Timber.d("Detected single file archive. ${fileUri.name}")
            handleFileUriAsSingleArchive(fileUri)
        } else {
            Timber.d("Detected standard file. ${fileUri.name}")
            handleFileUriAsStandardFile(fileUri)
        }
    }

    private fun traverseDirectoryEntries(rootUri: Uri): Observable<FileUri> = Observable.create { emitter ->
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
                            val uri = file.uri
                            val fileName = file.name
                            val size = file.length()
                            val mimeType = file.type
                            val parentName = file.parentFile?.name

                            if (fileName != null && mimeType != null) {
                                emitter.onNext(FileUri(uri, fileName, size, mimeType, parentName))
                            }
                            null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }

        emitter.onComplete()
    }

    private fun handleFileUriAsSingleArchive(file: FileUri): StorageFile {
        ZipInputStream(context.contentResolver.openInputStream(file.uri)).use {
            val entry = it.nextEntry

            Timber.d("Processing zipped entry: ${entry.name}")

            val serial = ISOScanner.extractSerial(entry.name, it)

            return StorageFile(entry.name, entry.size, entry.crc.toStringCRC32(), serial, file.uri, file.parent)
        }
    }

    private fun handleFileUriAsStandardFile(file: FileUri): StorageFile {
        val crc32 = if (file.size < MAX_SIZE_CRC32) {
            context.contentResolver.openInputStream(file.uri)?.calculateCrc32()
        } else {
            null
        }

        val serial = context.contentResolver.openInputStream(file.uri)?.let { inputStream ->
            ISOScanner.extractSerial(file.name, inputStream)
        }

        Timber.d("Detected file: $id, name: ${file.name}, crc: $crc32")

        return StorageFile(file.name, file.size, crc32, serial, file.uri, file.parent)
    }

    private fun isZipped(mimeType: String) = mimeType == ZIP_MIME_TYPE

    private fun isSingleArchive(uri: Uri): Boolean {
        val zipInputStream = ZipInputStream(context.contentResolver.openInputStream(uri))
        return LocalStorageUtils.isSingleArchive(zipInputStream)
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val gameFile = LocalStorageUtils.getCacheFileForGame(SAF_CACHE_SUBFOLDER, context, game)
        if (gameFile.exists()) {
            return@fromCallable gameFile
        }

        val mimeType = context.contentResolver.getType(game.fileUri)!!

        if (isZipped(mimeType) && isSingleArchive(game.fileUri)) {
            val stream = ZipInputStream(context.contentResolver.openInputStream(game.fileUri))
            LocalStorageUtils.extractFirstGameFromZipInputStream(stream, gameFile)
        } else {
            val stream = context.contentResolver.openInputStream(game.fileUri)!!
            LocalStorageUtils.copyInputStreamToFile(stream, gameFile)
        }
        gameFile
    }

    private data class FileUri(val uri: Uri, val name: String, val size: Long, val mime: String, val parent: String?)

    companion object {
        const val SAF_CACHE_SUBFOLDER = "storage-framework-games"
        const val ZIP_MIME_TYPE = "application/zip"

        const val MAX_SIZE_CRC32 = 500_000_000
    }
}
