package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.http.FileContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.common.kotlin.calculateMd5
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat

class SaveSyncManagerImpl(
    private val appContext: Context,
    private val directoriesManager: DirectoriesManager,
) : SaveSyncManager() {
    private var lastSyncTimestamp: Long by SharedPreferencesDelegates.LongDelegate(
        SharedPreferencesHelper.getSharedPreferences(appContext),
        appContext.getString(com.swordfish.lemuroid.lib.R.string.pref_key_last_save_sync),
        0L,
    )

    override fun getProvider(): String = "Google Drive"

    override fun getSettingsActivity(): Class<out Activity>? = ActivateGoogleDriveActivity::class.java

    override fun isSupported(): Boolean = true

    override fun isConfigured(): Boolean = GoogleSignIn.getLastSignedInAccount(appContext) != null

    override fun getLastSyncInfo(): String {
        val dateString =
            if (lastSyncTimestamp > 0) {
                SimpleDateFormat.getDateTimeInstance().format(lastSyncTimestamp)
            } else {
                "-"
            }
        return appContext.getString(R.string.gdrive_last_sync_completed, dateString)
    }

    override fun getConfigInfo(): String {
        val email = GoogleSignIn.getLastSignedInAccount(appContext)?.email
        return if (email != null) {
            appContext.getString(R.string.gdrive_connected_summary, email)
        } else {
            appContext.getString(R.string.gdrive_connected_none_summary)
        }
    }

    override suspend fun sync(cores: Set<CoreID>): Unit =
        withContext(Dispatchers.IO) {
            synchronized(SYNC_LOCK) {
                val saveSyncResult =
                    runCatching {
                        performSaveSyncForCores(cores)
                    }

                saveSyncResult.onFailure {
                    Timber.e(it, "Error while performing save sync.")
                }
            }
        }

    private fun performSaveSyncForCores(cores: Set<CoreID>) {
        val drive = DriveFactory(appContext).create() ?: return

        syncLocalAndRemoteFolder(
            drive,
            getOrCreateAppDataFolder("saves"),
            directoriesManager.getSavesDirectory(),
            null,
        )

        if (cores.isNotEmpty()) {
            syncLocalAndRemoteFolder(
                drive,
                getOrCreateAppDataFolder("states"),
                directoriesManager.getStatesDirectory(),
                cores.map { it.coreName }.toSet(),
            )
            syncLocalAndRemoteFolder(
                drive,
                getOrCreateAppDataFolder("state-previews"),
                directoriesManager.getStatesPreviewDirectory(),
                cores.map { it.coreName }.toSet(),
            )
        }

        lastSyncTimestamp = System.currentTimeMillis()
    }

    override fun computeSavesSpace() = getSizeHumanReadable(directoriesManager.getSavesDirectory())

    override fun computeStatesSpace(core: CoreID) =
        getSizeHumanReadable(File(directoriesManager.getStatesDirectory(), core.coreName))

    private fun getSizeHumanReadable(directory: File): String {
        val size =
            directory.walkBottomUp()
                .fold(0L) { acc, file -> acc + file.length() }
        return android.text.format.Formatter.formatShortFileSize(appContext, size)
    }

    private fun syncLocalAndRemoteFolder(
        drive: Drive,
        remoteFolderId: String,
        localFolder: File,
        prefixes: Set<String>?,
    ) {
        val remoteFiles = getRemoteFiles(drive, remoteFolderId)
        val remoteFilesMap = buildRemoteFileMap(remoteFiles)
        val localFilesMap = buildLocalFileMap(localFolder)

        getFilteredKeys(remoteFilesMap.keys + localFilesMap.keys, prefixes).forEach {
            handleFileSync(drive, remoteFolderId, localFolder, remoteFilesMap[it], localFilesMap[it])
        }
    }

    private fun getFilteredKeys(
        keys: Set<String>,
        prefixes: Set<String>?,
    ): Set<String> {
        if (prefixes == null) return keys
        return keys.filter { key -> prefixes.any { key.startsWith(it) } }.toSet()
    }

    private fun handleFileSync(
        drive: Drive,
        remoteParentFolderId: String,
        localParentFolder: File,
        remoteFile: com.google.api.services.drive.model.File?,
        localFile: File?,
    ) {
        Timber.i("Handling file pair: $localFile $remoteFile")

        runCatching {
            if (remoteFile != null && localFile == null) {
                onRemoteOnly(localParentFolder, remoteFile, drive)
            } else if (remoteFile == null && localFile != null) {
                onLocalOnly(remoteParentFolderId, localFile, localParentFolder, drive)
            } else if (remoteFile != null && localFile != null) {
                if (areFileDifferent(remoteFile, localFile)) {
                    if (remoteFile.modifiedTime.value < localFile.lastModified()) {
                        onLocalUpdated(localFile, drive, remoteFile)
                    } else if (remoteFile.modifiedTime.value > localFile.lastModified()) {
                        onRemoteUpdated(drive, remoteFile, localFile)
                    }
                }
            }
        }
    }

    private fun areFileDifferent(
        remoteFile: com.google.api.services.drive.model.File,
        localFile: File,
    ): Boolean {
        if (remoteFile.modifiedTime.value == localFile.lastModified()) {
            return false
        }

        if (remoteFile.size.toLong() != localFile.length()) {
            return true
        }

        return remoteFile.md5Checksum != localFile.calculateMd5()
    }

    private fun onLocalUpdated(
        localFile: File,
        drive: Drive,
        remoteFile: com.google.api.services.drive.model.File,
    ) {
        Timber.i("Local file updated $localFile")

        val mediaContent = FileContent("application/x-binary", localFile)
        val metadata = com.google.api.services.drive.model.File()
        metadata.modifiedTime = DateTime(localFile.lastModified())
        drive.files().update(remoteFile.id, metadata, mediaContent)
            .execute()
    }

    private fun onLocalOnly(
        remoteParentFolderId: String,
        localFile: File,
        localParentFolder: File,
        drive: Drive,
    ) {
        Timber.i("Local-only file detected $localFile")

        val metadata = com.google.api.services.drive.model.File()
        metadata.parents = listOf(remoteParentFolderId)
        metadata.name = localFile.name
        metadata.appProperties =
            mapOf(
                GDRIVE_PROPERTY_LOCAL_PATH to
                    localFile.toRelativeString(
                        localParentFolder,
                    ),
            )
        metadata.modifiedTime = DateTime(localFile.lastModified())
        val mediaContent = FileContent("application/x-binary", localFile)
        drive.files().create(metadata, mediaContent)
            .setFields("id")
            .execute()
    }

    private fun onRemoteOnly(
        localParentFolder: File,
        remoteFile: com.google.api.services.drive.model.File,
        drive: Drive,
    ) {
        Timber.i("Remote only file detected $remoteFile")
        val outputFile =
            File(
                localParentFolder,
                remoteFile.appProperties[GDRIVE_PROPERTY_LOCAL_PATH]!!,
            ).apply {
                parentFile?.mkdirs()
            }
        downloadToLocal(drive, remoteFile, outputFile)
    }

    private fun onRemoteUpdated(
        drive: Drive,
        remoteFile: com.google.api.services.drive.model.File,
        localFile: File,
    ) {
        Timber.i("Remote file updated $remoteFile")
        downloadToLocal(drive, remoteFile, localFile)
    }

    private fun downloadToLocal(
        drive: Drive,
        remoteFile: com.google.api.services.drive.model.File,
        localFile: File,
    ) {
        if (remoteFile.size == 0) return
        Timber.i("Downloading file to $localFile")
        drive.files()
            .get(remoteFile.id)
            .executeMediaAndDownloadTo(localFile.outputStream())
        localFile.setLastModified(remoteFile.modifiedTime.value)
    }

    private fun buildRemoteFileMap(
        remoteFiles: Sequence<com.google.api.services.drive.model.File>,
    ): Map<String, com.google.api.services.drive.model.File> {
        return remoteFiles
            .filter { it.appProperties?.get(GDRIVE_PROPERTY_LOCAL_PATH) != null }
            .map { it.appProperties?.get(GDRIVE_PROPERTY_LOCAL_PATH)!! to it }
            .toMap()
    }

    private fun buildLocalFileMap(folder: File): Map<String, File> {
        return folder
            .walkBottomUp()
            .filter { it.exists() && !it.isDirectory && it.length() > 0 }
            .map { it.toRelativeString(folder) to it }
            .toMap()
    }

    private fun getOrCreateAppDataFolder(folderName: String): String {
        val drive =
            DriveFactory(appContext).create()
                ?: throw UnsupportedOperationException()

        val query =
            drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$folderName' and mimeType = 'application/vnd.google-apps.folder'")
                .setFields("files(id)")
                .execute()

        if (query.files.size > 0) {
            return query.files[0].id
        }

        val metadata = com.google.api.services.drive.model.File()
        metadata.parents = listOf("appDataFolder")
        metadata.name = folderName
        metadata.mimeType = "application/vnd.google-apps.folder"

        val file =
            drive.files().create(metadata)
                .setFields("id")
                .execute()

        return file.id
    }

    private fun getRemoteFiles(
        drive: Drive,
        folderId: String,
    ): Sequence<com.google.api.services.drive.model.File> {
        var pageToken: String? = null
        return sequence {
            do {
                val query =
                    "'$folderId' in parents and trashed = false and mimeType = 'application/x-binary'"

                val fields =
                    "nextPageToken, " +
                        "files(id, name, size, appProperties, modifiedTime, parents, md5Checksum)"

                val result =
                    drive.files().list()
                        .setPageSize(500)
                        .setSpaces("appDataFolder")
                        .setQ(query)
                        .setFields(fields)
                        .setPageToken(pageToken)
                        .execute()

                yieldAll(result.files)
                pageToken = result.nextPageToken
            } while (pageToken != null)
        }
    }

    companion object {
        const val GDRIVE_PROPERTY_LOCAL_PATH = "localPath"
        private val SYNC_LOCK = Object()
    }
}
