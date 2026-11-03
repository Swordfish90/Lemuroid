package come.asg.lemudroid.app.shared.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.documentfile.provider.DocumentFile
import come.asg.lemudroid.app.shared.library.LibraryIndexScheduler
import come.asg.lemudroid.app.utils.android.displayErrorDialog
import come.asg.lemudroid.R
import come.asg.lemudroid.lib.android.RetrogradeActivity
import come.asg.lemudroid.lib.preferences.SharedPreferencesHelper
import come.asg.lemudroid.lib.storage.DirectoriesManager
import come.asg.lemudroid.lib.R as LibR
import java.io.File
import javax.inject.Inject

/** Selects the user-owned root for ROMs, saves, states, and state previews. */
class DataFolderPickerLauncher : RetrogradeActivity() {
    @Inject
    lateinit var directoriesManager: DirectoriesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.data_folder_access_title)
                    .setMessage(R.string.data_folder_access_message)
                    .setPositiveButton(R.string.data_folder_access_open_settings) { _, _ -> requestAllFilesAccess() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
                    .setOnCancelListener { finish() }
                    .show()
            } else {
                launchFolderPicker()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when (requestCode) {
            REQUEST_CODE_ALL_FILES_ACCESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    launchFolderPicker()
                } else {
                    displayErrorDialog(
                        getString(R.string.data_folder_access_denied),
                        getString(android.R.string.ok),
                    ) { finish() }
                }
            }
            REQUEST_CODE_PICK_FOLDER -> {
                if (resultCode == Activity.RESULT_OK) resultData?.data?.let { saveSelection(it) }
                finish()
            }
        }
    }

    private fun requestAllFilesAccess() {
        val appSettingsIntent =
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
        try {
            startActivityForResult(appSettingsIntent, REQUEST_CODE_ALL_FILES_ACCESS)
        } catch (_: Exception) {
            runCatching {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
                    REQUEST_CODE_ALL_FILES_ACCESS,
                )
            }.onFailure {
                displayErrorDialog(
                    getString(R.string.data_folder_access_settings_unavailable),
                    getString(android.R.string.ok),
                ) { finish() }
            }
        }
    }

    private fun launchFolderPicker() {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
        runCatching { startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER) }
            .onFailure {
                displayErrorDialog(
                    getString(R.string.dialog_saf_not_found, directoriesManager.getDataDirectory()),
                    getString(R.string.ok),
                ) { finish() }
            }
    }

    private fun saveSelection(uri: Uri) {
        val oldRoot = directoriesManager.getDataDirectory()
        val flags = resultFlags()
        runCatching { contentResolver.takePersistableUriPermission(uri, flags) }

        val preferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
        preferences.edit().putString(getString(LibR.string.pref_key_app_data_folder), uri.toString()).apply()

        DocumentFile.fromTreeUri(this, uri)?.let { root ->
            listOf("roms", "saves", "states", "state-previews").forEach { name ->
                if (root.findFile(name) == null) root.createDirectory(name)
            }
        }

        val newRoot = directoriesManager.getDataDirectory()
        if (oldRoot.absolutePath != newRoot.absolutePath && oldRoot.exists()) {
            oldRoot.listFiles()?.forEach { source ->
                runCatching { source.copyRecursively(File(newRoot, source.name), overwrite = false) }
            }
        }

        LibraryIndexScheduler.scheduleLibrarySync(applicationContext)
    }

    private fun resultFlags(): Int =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1
        private const val REQUEST_CODE_ALL_FILES_ACCESS = 2

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, DataFolderPickerLauncher::class.java))
        }
    }
}
