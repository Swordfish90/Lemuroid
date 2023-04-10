package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
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
                    updateInternalStorage(safDirectory)

                    // now copy from internal to saf
                    updateRemoteStorage(safDirectory)
                }
                lastSyncTimestamp = System.currentTimeMillis()
            }

            saveSyncResult.onFailure {
                Timber.e(it, "Error while performing save sync.")
            }
        }
    }

    private fun updateInternalStorage(safDirectory: DocumentFile) {
        val saveFiles = safDirectory.listFiles()

        for (saveFile in saveFiles) {
            if (!saveFile.name.isNullOrEmpty()) {
                val internalTarget = getInternalSaveFile(saveFile.name!!)
                if (internalTarget != null) {
                    if (internalTarget.lastModified() < saveFile.lastModified()) {
                        copyFromSafToInternal(saveFile, internalTarget)
                    }
                } else {
                    val internalDir = File(appContext.getExternalFilesDir(null), "saves");
                    val newTarget = File(internalDir, saveFile.name)
                    if (newTarget.createNewFile()) {
                        copyFromSafToInternal(saveFile, newTarget)
                    } else {
                        Timber.e("Could not create new file in internal storage")
                    }
                }
            } else {
                Timber.tag("SAF to Internal").d("Error: Remote file does not have a name!")
            }
        }
    }

    private fun updateRemoteStorage(safDirectory: DocumentFile) {
        // todo: check if there is a "saves"-constant
        val internalSavefiles = File(appContext.getExternalFilesDir(null), "saves").listFiles()

        if (internalSavefiles != null) {
            for (internalFile in internalSavefiles) {
                val safTarget = safDirectory.findFile(internalFile.name)
                if (safTarget != null) {
                    if (safTarget.lastModified() < internalFile.lastModified()) {
                        copyFromInternalToSaf(safTarget, internalFile)
                    }
                } else {
                    val newTarget = safDirectory.createFile("application/octet-stream", internalFile.name)
                    if (newTarget != null) {
                        copyFromInternalToSaf(newTarget, internalFile)
                    }
                }
            }
        }
    }

    private fun getInternalSaveFile(filename: String): File? {
        val saves = File(appContext.getExternalFilesDir(null), "saves")
        saves.mkdirs()
        for (i in saves.listFiles()!!) {
            if (i.name.equals(filename)) {
                return File(saves, i.name)
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


    override fun computeSavesSpace() = ""

    override fun computeStatesSpace(coreID: CoreID) = ""


    companion object {
        const val GDRIVE_PROPERTY_LOCAL_PATH = "localPath"
        private val SYNC_LOCK = Object()
    }

}
