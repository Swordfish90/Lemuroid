package com.swordfish.lemuroid.app.shared.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.saves.SavesBackupManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SavesBackupLauncher : RetrogradeActivity() {

    @Inject
    lateinit var directoriesManager: DirectoriesManager

    private val savesBackupManager by lazy { SavesBackupManager(directoriesManager) }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            when (intent.getStringExtra(EXTRA_ACTION)) {
                ACTION_EXPORT -> launchExportPicker()
                ACTION_IMPORT -> launchImportPicker()
                else -> finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun launchExportPicker() {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
                putExtra(Intent.EXTRA_TITLE, DEFAULT_BACKUP_FILENAME)
            }
            startActivityForResult(intent, REQUEST_CODE_EXPORT)
        } catch (e: Exception) {
            showError(getString(R.string.saves_backup_error_no_file_picker))
        }
    }

    private fun launchImportPicker() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            startActivityForResult(intent, REQUEST_CODE_IMPORT)
        } catch (e: Exception) {
            showError(getString(R.string.saves_backup_error_no_file_picker))
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode != Activity.RESULT_OK) {
            finish()
            return
        }

        val uri = resultData?.data
        if (uri == null) {
            finish()
            return
        }

        when (requestCode) {
            REQUEST_CODE_EXPORT -> performExport(uri)
            REQUEST_CODE_IMPORT -> performImport(uri)
            else -> finish()
        }
    }

    private fun performExport(uri: Uri) {
        scope.launch {
            val result = savesBackupManager.exportSaves(contentResolver, uri)
            when (result) {
                is SavesBackupManager.BackupResult.Success -> {
                    displayToast(R.string.saves_backup_export_success)
                }
                is SavesBackupManager.BackupResult.Error -> {
                    displayToast(R.string.saves_backup_export_error)
                }
            }
            finish()
        }
    }

    private fun performImport(uri: Uri) {
        scope.launch {
            val result = savesBackupManager.importSaves(contentResolver, uri)
            when (result) {
                is SavesBackupManager.BackupResult.Success -> {
                    displayToast(R.string.saves_backup_import_success)
                }
                is SavesBackupManager.BackupResult.Error -> {
                    displayToast(R.string.saves_backup_import_error)
                }
            }
            finish()
        }
    }

    private fun showError(message: String) {
        displayErrorDialog(message, getString(R.string.ok)) { finish() }
    }

    companion object {
        private const val REQUEST_CODE_EXPORT = 100
        private const val REQUEST_CODE_IMPORT = 101
        private const val EXTRA_ACTION = "action"
        private const val ACTION_EXPORT = "export"
        private const val ACTION_IMPORT = "import"
        private const val DEFAULT_BACKUP_FILENAME = "fullroid_saves_backup.zip"

        fun launchExport(context: Context) {
            context.startActivity(Intent(context, SavesBackupLauncher::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_EXPORT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        fun launchImport(context: Context) {
            context.startActivity(Intent(context, SavesBackupLauncher::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_IMPORT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
