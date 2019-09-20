package com.codebutler.retrograde.app.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import androidx.fragment.app.FragmentActivity
import com.codebutler.retrograde.R
import java.io.File
import java.io.InputStream

class StorageFrameworkPickerLauncher : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val preferenceKey = getString(R.string.pref_key_extenral_folder)
            PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
                this.putString(preferenceKey, resultData?.data.toString())
                this.apply()
            }
        }
        finish()
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1

        fun pickFolder(context: Context) = context.startActivity(Intent(context, StorageFrameworkPickerLauncher::class.java))
    }
}
