package come.asg.ominoy.app.shared.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import come.asg.ominoy.R
import come.asg.ominoy.app.shared.library.LibraryIndexScheduler
import come.asg.ominoy.app.utils.android.displayErrorDialog
import come.asg.ominoy.lib.android.RetrogradeActivity
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import come.asg.ominoy.lib.storage.DirectoriesManager
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
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
            val preferenceKey = getString(come.asg.ominoy.lib.R.string.pref_key_extenral_folder)

            val currentValue: String? = sharedPreferences.getString(preferenceKey, null)
            val newValue = resultData?.data

            if (newValue != null && saveSelection(newValue, resultData?.flags ?: 0, currentValue)) {
                sharedPreferences.edit().putString(preferenceKey, newValue.toString()).apply()
                // Re-selecting the same folder must still rescan it. This is
                // needed after an earlier provider listing failed or after ROMs
                // were copied into the folder later.
                LibraryIndexScheduler.scheduleLibrarySync(applicationContext)
            } else if (newValue != null) {
                Toast
                    .makeText(
                        this,
                        "Could not access that folder. Please choose a readable ROM folder.",
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }
        finish()
    }

    private fun saveSelection(
        uri: Uri,
        resultFlags: Int,
        previousValue: String?,
    ): Boolean {
        val permissionFlags =
            resultFlags and
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (permissionFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return false

        var tookGrant = false
        return runCatching {
            contentResolver.takePersistableUriPermission(uri, permissionFlags)
            tookGrant = true
            val folder = DocumentFile.fromTreeUri(this, uri) ?: error("Selected folder is unavailable")
            require(folder.exists() && folder.isDirectory && folder.canRead()) {
                "Selected folder is unavailable or unreadable"
            }

            if (previousValue != null && previousValue != uri.toString()) {
                val previousUri = Uri.parse(previousValue)
                contentResolver.persistedUriPermissions
                    .firstOrNull { it.uri == previousUri }
                    ?.let { permission ->
                        var oldFlags = 0
                        if (permission.isReadPermission) oldFlags = oldFlags or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        if (permission.isWritePermission) oldFlags = oldFlags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        if (oldFlags != 0) {
                            runCatching { contentResolver.releasePersistableUriPermission(previousUri, oldFlags) }
                        }
                    }
            }
            true
        }.getOrElse {
            if (tookGrant && previousValue != uri.toString()) {
                runCatching { contentResolver.releasePersistableUriPermission(uri, permissionFlags) }
            }
            false
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, StorageFrameworkPickerLauncher::class.java))
        }
    }
}
