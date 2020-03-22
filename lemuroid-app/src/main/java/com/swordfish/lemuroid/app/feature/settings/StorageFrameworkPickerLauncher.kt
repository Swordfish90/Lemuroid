package com.swordfish.lemuroid.app.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.feature.library.LibraryIndexWork
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import javax.inject.Inject

class StorageFrameworkPickerLauncher : RetrogradeActivity() {

    @Inject lateinit var directoriesManager: DirectoriesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val preferenceKey = getString(R.string.pref_key_extenral_folder)

            val currentValue: String? = sharedPreferences.getString(preferenceKey, null)
            val newValue = resultData?.data

            if (newValue.toString() != currentValue) {
                clearPreviousPersistentUris()

                newValue?.let {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                sharedPreferences.edit().apply {
                    this.putString(preferenceKey, newValue.toString())
                    this.apply()
                }
            }

            startLibraryIndexWork()
        }
        finish()
    }

    private fun startLibraryIndexWork() {
        LibraryIndexWork.enqueueUniqueWork(applicationContext)
    }

    private fun clearPreviousPersistentUris() {
        contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .forEach {
                contentResolver.releasePersistableUriPermission(it.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, StorageFrameworkPickerLauncher::class.java))
        }
    }
}
