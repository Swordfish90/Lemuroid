package come.asg.ominoy.lib.storage.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.leanback.preference.LeanbackPreferenceFragment
import come.asg.ominoy.common.kotlin.extractEntryToFile
import come.asg.ominoy.common.kotlin.isZipped
import come.asg.ominoy.common.kotlin.writeToFile
import come.asg.ominoy.lib.R
import come.asg.ominoy.lib.library.db.entity.DataFile
import come.asg.ominoy.lib.library.db.entity.Game
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import come.asg.ominoy.lib.storage.BaseStorageFile
import come.asg.ominoy.lib.storage.RomFiles
import come.asg.ominoy.lib.storage.StorageFile
import come.asg.ominoy.lib.storage.StorageProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.ArrayDeque
import java.util.zip.ZipInputStream

class StorageAccessFrameworkProvider(
    private val context: Context,
) : StorageProvider {
    override val id: String = "access_framework"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("content")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listBaseStorageFiles(): Flow<List<BaseStorageFile>> =
        getExternalFolder()?.let { folder ->
            traverseDirectoryEntries(Uri.parse(folder))
        } ?: emptyFlow()

    override fun getStorageFile(baseStorageFile: BaseStorageFile): StorageFile? =
        DocumentFileParser.parseDocumentFile(context, baseStorageFile)

    private fun getExternalFolder(): String? {
        val prefString = context.getString(R.string.pref_key_extenral_folder)
        val preferenceManager = SharedPreferencesHelper.getLegacySharedPreferences(context)
        return preferenceManager.getString(prefString, null)
    }

    /**
     * Walk the selected tree through DocumentFile instead of rebuilding child
     * query URIs from document IDs. Some Android document providers expose a
     * valid tree URI but do not correctly handle buildChildDocumentsUriUsingTree
     * at every level, which made the old implementation silently find no ROMs.
     */
    private fun traverseDirectoryEntries(rootUri: Uri): Flow<List<BaseStorageFile>> =
        flow {
            val root = DocumentFile.fromTreeUri(context, rootUri)
            if (root == null || !root.isDirectory) {
                Timber.w("Selected ROM tree is unavailable or is not a directory: $rootUri")
                return@flow
            }

            val directories = ArrayDeque<DocumentFile>()
            directories.add(root)
            while (directories.isNotEmpty()) {
                val directory = directories.removeFirst()
                val files =
                    runCatching { directory.listFiles().toList() }
                        .onFailure { Timber.e(it, "Error while listing SAF directory: ${directory.uri}") }
                        .getOrDefault(emptyList())

                val romFiles = mutableListOf<BaseStorageFile>()
                files.forEach { file ->
                    val name = file.name ?: return@forEach
                    if (name.isBlank() || name.startsWith(".")) return@forEach
                    if (file.isDirectory) {
                        directories.addLast(file)
                    } else if (file.isFile) {
                        romFiles += BaseStorageFile(name, file.length(), file.uri, file.uri.path)
                    }
                }
                if (romFiles.isNotEmpty()) emit(romFiles)
            }
        }

    override fun getGameRomFiles(
        game: Game,
        dataFiles: List<DataFile>,
        allowVirtualFiles: Boolean,
    ): RomFiles {
        val originalDocumentUri = Uri.parse(game.fileUri)
        val originalDocument = DocumentFile.fromSingleUri(context, originalDocumentUri)!!

        val isZipped = originalDocument.isZipped() && originalDocument.name != game.fileName

        return when {
            isZipped && dataFiles.isEmpty() -> getGameRomFilesZipped(game, originalDocument)
            allowVirtualFiles -> getGameRomFilesVirtual(game, dataFiles)
            else -> getGameRomFilesStandard(game, dataFiles, originalDocument)
        }
    }

    private fun getGameRomFilesStandard(
        game: Game,
        dataFiles: List<DataFile>,
        originalDocument: DocumentFile,
    ): RomFiles {
        val gameEntry = getGameRomStandard(game, originalDocument)
        val dataEntries = dataFiles.map { getDataFileStandard(game, it) }
        return RomFiles.Standard(listOf(gameEntry) + dataEntries)
    }

    private fun getGameRomFilesZipped(
        game: Game,
        originalDocument: DocumentFile,
    ): RomFiles {
        val cacheFile =
            GameCacheUtils.getCacheFileForGame(
                SAF_CACHE_SUBFOLDER,
                context,
                game,
                sourceMetadata(originalDocument),
            )
        if (cacheFile.exists()) {
            return RomFiles.Standard(listOf(cacheFile))
        }

        val stream =
            ZipInputStream(
                context.contentResolver.openInputStream(originalDocument.uri),
            )
        stream.extractEntryToFile(game.fileName, cacheFile)
        return RomFiles.Standard(listOf(cacheFile))
    }

    private fun getGameRomFilesVirtual(
        game: Game,
        dataFiles: List<DataFile>,
    ): RomFiles {
        val gameEntry = getGameRomVirtual(game)
        val dataEntries = dataFiles.map { getDataFileVirtual(it) }
        return RomFiles.Virtual(listOf(gameEntry) + dataEntries)
    }

    private fun getDataFileVirtual(dataFile: DataFile): RomFiles.Virtual.Entry =
        RomFiles.Virtual.Entry(
            "$VIRTUAL_FILE_PATH/${dataFile.fileName}",
            context.contentResolver.openFileDescriptor(Uri.parse(dataFile.fileUri), "r")!!,
        )

    private fun getDataFileStandard(
        game: Game,
        dataFile: DataFile,
    ): File {
        val cacheFile =
            GameCacheUtils.getDataFileForGame(
                SAF_CACHE_SUBFOLDER,
                context,
                game,
                dataFile,
            )

        if (cacheFile.exists()) {
            return cacheFile
        }

        val stream = context.contentResolver.openInputStream(Uri.parse(dataFile.fileUri))!!
        stream.writeToFile(cacheFile)
        return cacheFile
    }

    private fun getGameRomVirtual(game: Game): RomFiles.Virtual.Entry =
        RomFiles.Virtual.Entry(
            "$VIRTUAL_FILE_PATH/${game.fileName}",
            context.contentResolver.openFileDescriptor(Uri.parse(game.fileUri), "r")!!,
        )

    private fun getGameRomStandard(
        game: Game,
        originalDocument: DocumentFile,
    ): File {
        val cacheFile =
            GameCacheUtils.getCacheFileForGame(
                SAF_CACHE_SUBFOLDER,
                context,
                game,
                sourceMetadata(originalDocument),
            )

        if (cacheFile.exists()) {
            return cacheFile
        }

        val stream = context.contentResolver.openInputStream(originalDocument.uri)!!
        stream.writeToFile(cacheFile)
        return cacheFile
    }

    override fun getInputStream(uri: Uri): InputStream? = context.contentResolver.openInputStream(uri)

    private fun sourceMetadata(document: DocumentFile): String = "${document.length()}:${document.lastModified()}"

    companion object {
        const val SAF_CACHE_SUBFOLDER = "storage-framework-games"
        const val VIRTUAL_FILE_PATH = "/virtual/file/path"
    }
}
