package com.swordfish.lemuroid.app.shared.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import javax.inject.Inject


class SAFSavePickerLauncher : RetrogradeActivity() {

    @Inject lateinit var directoriesManager: DirectoriesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                this.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                this.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                this.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                this.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            try {
                startActivityForResult(intent, REQUEST_CODE_PICK_SAVEGAMEFOLDER)
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

        if (requestCode == REQUEST_CODE_PICK_SAVEGAMEFOLDER && resultCode == Activity.RESULT_OK) {
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
            val preferenceKey = getString(R.string.pref_key_external_save_folder)

            val currentValue: String? = sharedPreferences.getString(preferenceKey, null)
            val newValue = resultData?.data

            if (newValue != null && newValue.toString() != currentValue) {
                updatePersistableUrisRW(newValue)

                sharedPreferences.edit().apply {
                    this.putString(preferenceKey, newValue.toString())
                    this.apply()
                }
            }
        }
        finish()
    }

    private fun updatePersistableUrisRW(uri: Uri) {

        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_CODE_PICK_SAVEGAMEFOLDER = 2

        fun pickSavegameFolder(context: Context) {
            context.startActivity(Intent(context, SAFSavePickerLauncher::class.java))
        }
    }
}
