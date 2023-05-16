package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat


class SaveSyncManagerImpl(
    private val appContext: Context,
    private val directoriesManager: DirectoriesManager
) : SaveSyncManager {

    private var lastSyncTimestamp: Long by SharedPreferencesDelegates.LongDelegate(
        SharedPreferencesHelper.getSharedPreferences(appContext),
        appContext.getString(R.string.pref_key_last_save_sync),
        0L
    )

    private var storageUri: String by SharedPreferencesDelegates.StringDelegate(
        SharedPreferencesHelper.getSharedPreferences(appContext),
        appContext.getString(R.string.pref_key_saf_uri),
        ActivateSAFActivity.PREF_KEY_STORAGE_URI_NONE
    )


    override fun getProvider(): String = appContext.getString(R.string.saf_save_sync_providername)

    override fun getSettingsActivity(): Class<out Activity>? = ActivateSAFActivity::class.java

    override fun isSupported(): Boolean = true

    override fun isConfigured(): Boolean {
        return storageUri != ActivateSAFActivity.PREF_KEY_STORAGE_URI_NONE
    }

    override fun getLastSyncInfo(): String {
        val dateString = if (lastSyncTimestamp > 0) {
            SimpleDateFormat.getDateTimeInstance().format(lastSyncTimestamp)
        } else {
            "-"
        }
        return appContext.getString(R.string.saf_last_sync_completed, dateString)
    }

    override fun getConfigInfo(): String {
        return storageUri
    }

    /**
     * Sync savegames.
     *
     * Todo: Sync states!
     */
    override suspend fun sync(cores: Set<CoreID>) {
        synchronized(SYNC_LOCK) {
            val saveSyncResult = runCatching {
                val safProviderUri = Uri.parse(storageUri)
                val safDirectory = DocumentFile.fromTreeUri(appContext.applicationContext, safProviderUri)

                if (safDirectory != null) {

                    // copy from saf to internal
                    updateInternalStorage(safDirectory, "saves")

                    // now copy from internal to saf
                    updateRemoteStorage(safDirectory, "saves")

                    //repeat for states
                    updateInternalStorage(safDirectory, "states")
                    updateRemoteStorage(safDirectory, "states")
                }
                lastSyncTimestamp = System.currentTimeMillis()
            }

            saveSyncResult.onFailure {
                Timber.e(it, "Error while performing save sync.")
            }
        }
    }

    private fun updateInternalStorage(safDirectory: DocumentFile, subdir: String) {
        val internalSaves = File(appContext.getExternalFilesDir(null), subdir)
        var safSubdir = safDirectory.findFile(subdir)
        if(safSubdir == null) {
            safSubdir = safDirectory.createDirectory(subdir)
        }

        if (safSubdir != null) {
            updateInternalStorageFolder(safSubdir, internalSaves)
        }
    }

    private fun updateInternalStorageFolder(currentSafTarget: DocumentFile, currentInternalFolder: File) {
        for (saveFile in currentSafTarget.listFiles()) {
            if(saveFile.isFile) {
                if (!saveFile.name.isNullOrEmpty()) {
                    val internalTarget = getFileFromFolder(currentInternalFolder, saveFile.name.toString())
                    if(internalTarget != null){
                        if (internalTarget.lastModified() < saveFile.lastModified()) {
                            copyFromSafToInternal(saveFile, internalTarget)
                        }
                    } else {
                        val newTarget = File(currentInternalFolder, saveFile.name)
                        if (newTarget.createNewFile()) {
                            copyFromSafToInternal(saveFile, newTarget)
                        } else {
                            Timber.e("Could not create new file in internal storage")
                        }
                    }
                } else {
                    Timber.tag("SAF to Internal").d("Error: Remote file does not have a name!")
                }
            } else {
                var internalFolder = getFileFromFolder(currentInternalFolder, saveFile.name.toString())
                if(internalFolder == null) {
                    internalFolder = File(currentInternalFolder.absolutePath+"/"+saveFile.name)
                    internalFolder.mkdirs()
                }

                updateInternalStorageFolder(saveFile, internalFolder)
            }
        }
    }

    private fun updateRemoteStorage(safDirectory: DocumentFile, subdir: String) {
        // todo: check if there is a "saves"-constant
        val internalSavefiles = File(appContext.getExternalFilesDir(null), subdir)

        var safSubdir = safDirectory.findFile(subdir)
        if(safSubdir == null) {
            safSubdir = safDirectory.createDirectory(subdir)
        }

        if (safSubdir != null) {
            updateRemoteStorageFolder(internalSavefiles, safSubdir)
        }
    }

    private fun updateRemoteStorageFolder(currentInternalFolder: File, currentSafTarget: DocumentFile) {
        val recursiveFiles = currentInternalFolder.listFiles()

        for (internalFile in recursiveFiles) {
            if(internalFile.isFile) {
                val safTarget = currentSafTarget.findFile(internalFile.name)
                if (safTarget != null) {
                    if (safTarget.lastModified() < internalFile.lastModified()) {
                        copyFromInternalToSaf(safTarget, internalFile)
                    }
                } else {
                    val newTarget = currentSafTarget.createFile("application/octet-stream", internalFile.name)
                    if (newTarget != null) {
                        copyFromInternalToSaf(newTarget, internalFile)
                    }
                }
            } else {
                var targetFolder = currentSafTarget.findFile(internalFile.name)
                if(targetFolder == null || !targetFolder.exists()){
                    targetFolder = currentSafTarget.createDirectory(internalFile.name)
                }

                if (targetFolder == null) {
                    Timber.e("SaveSync", "Target is null. Skipping.")
                    return
                }
                updateRemoteStorageFolder( internalFile, targetFolder)
            }
        }
    }

    private fun getFileFromFolder(folder: File, filename: String): File? {
        if(!folder.isDirectory){
            return null
        }

        for (i in folder.listFiles()) {
            if (i.name.equals(filename)) {
                return i
            }
        }
        return null
    }

    /**
     * Copy a file from the provided DocumentFile to File via copyFile().
     * File will get an updated timestamp, matching the older DocumentFile.
     * File will have a backdated timestamp. This is so that the sync-client will
     * not "backsync" the "newer" file in Internal Storage back to SAF even if both are identical.
     */
    private fun copyFromSafToInternal(saf: DocumentFile, internal: File) {
        Log.e("SaveSync", "To: "+internal.name)
        Log.e("SaveSync", "From: "+saf.name)

        val output: OutputStream = FileOutputStream(internal)
        val input: InputStream? = appContext.contentResolver.openInputStream(saf.uri)
        copyFile(input, output)
        // update last modified timestamp to match (this one backdates the file)
        internal.setLastModified(saf.lastModified())
    }

    /**
     * Copy a file from the provided File to DocumentFile via copyFile().
     * File will get an updated timestamp, matching the (new) DocumentFile.
     * For the reasoning, see copyFromSafToInternal()
     */
    private fun copyFromInternalToSaf(saf: DocumentFile, internal: File) {
        Log.e("SaveSync", "Copy to: "+saf.name)
        val output: OutputStream? = appContext.contentResolver.openOutputStream(saf.uri)
        val input: InputStream = FileInputStream(internal)


        copyFile(input, output)

        // update last modified timestamp to match
        internal.setLastModified(saf.lastModified())
    }


    /**
     * This function writes from input to output.
     * Null-checks are performed and caught.
     */
    private fun copyFile(inputStream: InputStream?, outputStream: OutputStream?) {
        if (inputStream == null) {
            Timber.d("SaveSyncManagerImpl: copyFile: Could not read source file!")
            return
        }
        if (outputStream == null) {
            Timber.d("SaveSyncManagerImpl: copyFile: Could not read target file!")
            return
        }

        inputStream.use { input ->
            outputStream.use { output ->
                // use 8k buffer for better performance
                val buf = ByteArray(8192)
                var len: Int
                while (input.read(buf).also { len = it } > 0) {
                    output.write(buf, 0, len)
                }
            }
        }
    }

    override fun computeSavesSpace(): String {
        var size = 0L
        val safProviderUri = Uri.parse(storageUri)
        val safDirectory = DocumentFile.fromTreeUri(appContext.applicationContext, safProviderUri)
        val saves = safDirectory?.findFile("saves")

        if (safDirectory != null) {
            size = saves?.let { getSpaceForDirectory(it) } ?: 0L
        }

        return Formatter.formatShortFileSize(appContext, size)
    }

    override fun computeStatesSpace(coreID: CoreID): String {
        var size = 0L

        val safProviderUri = Uri.parse(storageUri)
        val safDirectory = DocumentFile.fromTreeUri(appContext.applicationContext, safProviderUri)
        val states = safDirectory?.findFile("states")
        val core = states?.findFile(coreID.coreName)

        if (safDirectory != null) {
            size = core?.let { getSpaceForDirectory(it) } ?: 0L
        }

        return Formatter.formatShortFileSize(appContext, size)
    }

    /**
     * Calculate the size for a given folder, and its subdirectories.
     * Only respects files, folders are not counted.
     */
    private fun getSpaceForDirectory(safDirectory: DocumentFile): Long {
        val files = safDirectory.listFiles()
        var size = 0L

        for (file in files) {
            if(file.isFile) {
                size += file.length()
            } else {
                size += getSpaceForDirectory(file)
            }
        }

        return size
    }


    companion object {
        private val SYNC_LOCK = Object()
    }
}
