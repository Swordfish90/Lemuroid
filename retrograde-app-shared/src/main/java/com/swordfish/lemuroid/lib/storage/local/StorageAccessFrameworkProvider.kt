package com.swordfish.lemuroid.lib.storage.local

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
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.util.zip.ZipEntry
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
            traverseDirectoryEntries(Uri.parse(folder)).map { handleFile(it) }
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

    private fun handleFile(file: DocumentFile): StorageFile {
        return if (isZipped(file)) {
            Timber.d("Detected zip file. ${file.name}")
            handleFileAsZipFile(file)
        } else {
            Timber.d("Detected standard file. ${file.name}")
            handleFileAsStandardFile(file)
        }
    }

    private fun handleFileAsZipFile(file: DocumentFile): StorageFile {
        val inputStream = context.contentResolver.openInputStream(file.uri)
        return ZipInputStream(inputStream).use {
            val gameEntry = LocalStorageUtils.findGameEntry(it, file.length())
            if (gameEntry != null) {
                Timber.d("Handing zip file as compressed game: ${file.name}")
                handleFileAsCompressedGame(file, gameEntry, it)
            } else {
                Timber.d("Handing zip file as standard: ${file.name}")
                handleFileAsStandardFile(file)
            }
        }
    }

    private fun handleFileAsCompressedGame(file: DocumentFile, entry: ZipEntry, zipInputStream: ZipInputStream): StorageFile {
        Timber.d("Processing zipped entry: ${entry.name}")

        val serial = ISOScanner.extractSerial(entry.name, zipInputStream)

        return StorageFile(entry.name, entry.size, entry.crc.toStringCRC32(), serial, file.uri, file.parentFile?.name)
    }

    private fun handleFileAsStandardFile(file: DocumentFile): StorageFile {
        val crc32 = if (file.length() < MAX_SIZE_CRC32) {
            context.contentResolver.openInputStream(file.uri)?.calculateCrc32()
        } else {
            null
        }

        val serial = context.contentResolver.openInputStream(file.uri)?.let { inputStream ->
            ISOScanner.extractSerial(file.name!!, inputStream)
        }

        Timber.d("Detected file: $id, name: ${file.name}, crc: $crc32")

        return StorageFile(file.name!!, file.length(), crc32, serial, file.uri, file.parentFile?.name)
    }

    private fun isZipped(file: DocumentFile) = file.type == ZIP_MIME_TYPE

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val cacheFile = LocalStorageUtils.getCacheFileForGame(SAF_CACHE_SUBFOLDER, context, game)
        if (cacheFile.exists()) {
            return@fromCallable cacheFile
        }

        val originalDocument = DocumentFile.fromSingleUri(context, game.fileUri)!!

        if (isZipped(originalDocument) && originalDocument.name != game.fileName) {
            val stream = ZipInputStream(context.contentResolver.openInputStream(originalDocument.uri))
            LocalStorageUtils.extractZipEntryToFile(stream, game.fileName, cacheFile)
        } else {
            val stream = context.contentResolver.openInputStream(game.fileUri)!!
            LocalStorageUtils.copyInputStreamToFile(stream, cacheFile)
        }
        cacheFile
    }

    companion object {
        const val SAF_CACHE_SUBFOLDER = "storage-framework-games"
        const val ZIP_MIME_TYPE = "application/zip"

        const val MAX_SIZE_CRC32 = 500_000_000
    }
}
