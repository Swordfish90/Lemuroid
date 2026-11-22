package com.swordfish.lemuroid.app.shared.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import javax.inject.Inject

class StorageFrameworkPickerLauncher : RetrogradeActivity() {
    @Inject
    lateinit var directoriesManager: DirectoriesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    this.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    this.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    this.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                    this.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                }
            try {
                startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER)
            } catch (e: Exception) {
                showStorageAccessFrameworkNotSupportedDialog()
            }
        }
    }

    private fun showStorageAccessFrameworkNotSupportedDialog() {
        val message = getString(R.string.dialog_saf_not_found, directoriesManager.getInternalRomsDirectory())
        val actionLabel = getString(R.string.ok)
        displayErrorDialog(message, actionLabel) { finish() }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val newValue = resultData?.data

            if (newValue != null) {
                takePersistableUriPermission(newValue)
                addFolderToPreferences(newValue)
                migrateOldSingleFolder()
            }

            startLibraryIndexWork()
        }
        finish()
    }

    private fun takePersistableUriPermission(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }.onFailure {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun addFolderToPreferences(uri: Uri) {
        val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
        val foldersKey = getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)
        val currentFolders = sharedPreferences.getStringSet(foldersKey, emptySet()) ?: emptySet()
        val updatedFolders = currentFolders.toMutableSet().apply { add(uri.toString()) }
        sharedPreferences.edit().putStringSet(foldersKey, updatedFolders).apply()

        val singleKey = getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)
        sharedPreferences.edit().putString(singleKey, uri.toString()).apply()
    }


    private fun migrateOldSingleFolder() {
        val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
        val singleKey = getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)
        val foldersKey = getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)

        val oldFolder = sharedPreferences.getString(singleKey, null)
        if (!oldFolder.isNullOrBlank()) {
            val currentFolders = sharedPreferences.getStringSet(foldersKey, emptySet()) ?: emptySet()
            if (!currentFolders.contains(oldFolder)) {
                val updatedFolders = currentFolders.toMutableSet().apply { add(oldFolder) }
                sharedPreferences.edit().putStringSet(foldersKey, updatedFolders).apply()
            }
        }
    }

    private fun startLibraryIndexWork() {
        LibraryIndexScheduler.scheduleLibrarySync(applicationContext)
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, StorageFrameworkPickerLauncher::class.java))
        }


        fun removeFolder(context: Context, folderUri: String) {
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(context)
            val foldersKey = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)
            val currentFolders = sharedPreferences.getStringSet(foldersKey, emptySet()) ?: emptySet()
            val updatedFolders = currentFolders.toMutableSet().apply { remove(folderUri) }
            sharedPreferences.edit().putStringSet(foldersKey, updatedFolders).apply()

            val singleKey = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)
            val newSingle = updatedFolders.firstOrNull() ?: ""
            sharedPreferences.edit().putString(singleKey, newSingle).apply()

            runCatching {
                val uri = Uri.parse(folderUri)
                context.contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }

            LibraryIndexScheduler.scheduleLibrarySync(context.applicationContext)
        }


        fun getFolders(context: Context): Set<String> {
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(context)
            val foldersKey = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)
            val folders = sharedPreferences.getStringSet(foldersKey, emptySet()) ?: emptySet()

            if (folders.isEmpty()) {
                val singleKey = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)
                val oldFolder = sharedPreferences.getString(singleKey, null)
                if (!oldFolder.isNullOrBlank()) {
                    val migrated = setOf(oldFolder)
                    sharedPreferences.edit().putStringSet(foldersKey, migrated).apply()
                    return migrated
                }
            }
            return folders
        }
    }
}
