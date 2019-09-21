package com.codebutler.retrograde.lib.storage.accessframework

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.preference.PreferenceManager
import com.codebutler.retrograde.common.db.asSequence
import com.codebutler.retrograde.common.kotlin.calculateCrc32
import com.codebutler.retrograde.common.kotlin.writeInputStream
import com.codebutler.retrograde.lib.R
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.library.metadata.GameMetadataProvider
import com.codebutler.retrograde.lib.storage.StorageFile
import com.codebutler.retrograde.lib.storage.StorageProvider
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

class StorageAccessFrameworkProvider(
    private val context: Context,
    override val metadataProvider: GameMetadataProvider
) : StorageProvider {

    override val id: String = "access_framework"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("content")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listFiles(): Single<Iterable<StorageFile>> = Single.fromCallable {
        getExternalFolder()?.let { traverseDirectoryEntries(Uri.parse(it)) } ?: listOf()
    }

    private fun getExternalFolder(): String? {
        val prefString = context.getString(R.string.pref_key_extenral_folder)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        return preferenceManager.getString(prefString, null)
    }

    fun traverseDirectoryEntries(rootUri: Uri): List<StorageFile> {
        val result = mutableListOf<StorageFile>()

        val contentResolver = context.contentResolver
        var currentNode = DocumentsContract
            .buildChildDocumentsUriUsingTree(rootUri, DocumentsContract
            .getTreeDocumentId(rootUri))

        // Keep track of our directory hierarchy
        val dirNodes = mutableListOf<Uri>()
        dirNodes.add(currentNode)

        while (dirNodes.isNotEmpty()) {
            currentNode = dirNodes.removeAt(0)

            val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE
            )

            Timber.d("Detected node uri: $currentNode")

            contentResolver.query(currentNode, projection, null, null, null)?.use { cursor ->
                cursor.asSequence().map {
                    val docId = it.getString(0)
                    val name = it.getString(1)
                    val mime = it.getString(2)
                    val size = it.getLong(3)

                    if (isDirectory(mime)) {
                        val newNode = DocumentsContract.buildChildDocumentsUriUsingTree(currentNode, docId)
                        dirNodes.add(newNode)

                        Timber.d("Detected subfolder: $id, name: $name")
                    } else {
                        val uri = DocumentsContract.buildDocumentUriUsingTree(rootUri, docId)
                        val crc32 = context.contentResolver.openInputStream(uri)?.calculateCrc32()?.toUpperCase()
                        result.add(StorageFile(name, size, crc32, uri))

                        Timber.d("Detected file: $id, name: $name, mime: $mime, crc: $crc32")
                    }
                }.toList()
            }
        }

        return result
    }

    private fun isDirectory(mimeType: String) = DocumentsContract.Document.MIME_TYPE_DIR == mimeType

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        context.contentResolver.openInputStream(game.fileUri)?.use { inputStream ->
            val outputDir = context.cacheDir
            File.createTempFile(ROM_CACHE_NAME, "tmp", outputDir)
                .writeInputStream(inputStream)
                .also { it.deleteOnExit() } // Clear cache file when vm terminates.
        }
    }

    override fun getGameSave(game: Game): Single<Optional<ByteArray>> {
        val saveFile = getSaveFile(game)
        return if (saveFile.exists()) {
            Single.just(saveFile.readBytes().toOptional())
        } else {
            Single.just(None)
        }
    }

    override fun setGameSave(game: Game, data: ByteArray): Completable = Completable.fromCallable {
        val saveFile = getSaveFile(game)
        saveFile.writeBytes(data)
    }

    private fun getSaveFile(game: Game): File {
        val retrogradeDir = File(Environment.getExternalStorageDirectory(), "retrograde")
        val savesDir = File(retrogradeDir, "saves")
        savesDir.mkdirs()
        return File(savesDir, "${game.fileName}.sram")
    }

    companion object {
        const val ROM_CACHE_NAME = "current_game"
    }
}
