package come.asg.ominoy.lib.storage

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import come.asg.ominoy.lib.R
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import java.io.File

class DirectoriesManager(private val appContext: Context) {
    private val saveFolderMirror by lazy { SaveFolderMirror(appContext, this) }
    private val legacyPreferences: SharedPreferences by lazy {
        SharedPreferencesHelper.getLegacySharedPreferences(appContext)
    }

    /** Imports the selected SAF save folder into path-based working storage before a game starts. */
    suspend fun importSelectedSaveFolder() = saveFolderMirror.importToLocal()

    /** Exports SRAM and save states to the selected SAF folder after local writes are complete. */
    suspend fun exportSelectedSaveFolder() = saveFolderMirror.exportToSaf()

    /** Copies existing local save files into the selected tree without replacing destination files. */
    suspend fun copyExistingSavesToSelectedFolder() = saveFolderMirror.copyExistingToSafWithoutOverwriting()

    suspend fun exportAchievementsToSelectedDataFolder() = saveFolderMirror.exportAchievementsToDataFolder()

    /** The SAF-selected data root when it is addressable as a normal primary-storage path. */
    fun getSelectedDataDirectory(): File? {
        val storedUri =
            legacyPreferences.getString(appContext.getString(R.string.pref_key_app_data_folder), null)
                ?: return null
        return runCatching {
            val documentId = DocumentsContract.getTreeDocumentId(Uri.parse(storedUri))
            if (!documentId.startsWith("primary:")) return@runCatching null
            val relativePath = documentId.removePrefix("primary:")
            File(Environment.getExternalStorageDirectory(), relativePath)
        }.getOrNull()
    }

    fun getDataDirectory(): File =
        (getSelectedDataDirectory() ?: appContext.getExternalFilesDir(null)!!).apply { mkdirs() }

    private fun getSelectedDataSubdirectory(name: String): File =
        File(getDataDirectory(), name).apply { mkdirs() }

    @Deprecated("Use the external states directory")
    fun getInternalStatesDirectory(): File =
        File(appContext.filesDir, "states").apply {
            mkdirs()
        }

    fun getCoresDirectory(): File =
        File(appContext.filesDir, "cores").apply {
            mkdirs()
        }

    fun getSystemDirectory(): File =
        File(appContext.filesDir, "system").apply {
            mkdirs()
        }

    fun getStatesDirectory(): File =
        getSelectedDataSubdirectory("states")

    fun getStatesPreviewDirectory(): File =
        getSelectedDataSubdirectory("state-previews")

    fun getSavesDirectory(): File =
        getSelectedDataSubdirectory("saves")

    fun getAchievementsDirectory(): File =
        getSelectedDataSubdirectory("achievements")

    fun getInternalRomsDirectory(): File =
        getSelectedDataSubdirectory("roms")
}
