package com.swordfish.lemuroid.app.shared.settings
    
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import javax.inject.Inject

class LocalSaveSyncPickerLauncher : RetrogradeActivity() {
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
        val message = getString(R.string.dialog_saf_not_found, directoriesManager.getSavesDirectory())
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
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
            val preferenceKey = getString(com.swordfish.lemuroid.lib.R.string.pref_key_local_save_sync_folder)

            val currentValue: String? = sharedPreferences.getString(preferenceKey, null)
            val newValue = resultData?.data

            if (newValue != null && newValue.toString() != currentValue) {
                updatePersistableUris(newValue)

                sharedPreferences.edit().apply {
                    this.putString(preferenceKey, newValue.toString())
                    this.apply()
                }
            }
        }
        finish()
    }

    private fun updatePersistableUris(uri: Uri) {
        // Release older permissions for local save sync if any exist, to avoid leaking.
        // For simplicity, we just grant the new one.
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 101

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, LocalSaveSyncPickerLauncher::class.java))
        }
    }
}
