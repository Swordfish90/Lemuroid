package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

class ActivateSAFActivity : AppCompatActivity() {

    companion object {
        const val PREF_KEY_STORAGE_URI_NONE = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activate_safactivity)

        openPicker()
    }

    private fun openPicker() {


        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            this.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            this.putExtra(Intent.EXTRA_LOCAL_ONLY, false)
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val targetUri = result?.data?.data

                    if (targetUri != null ) {
                        setStorageUri(targetUri.toString())
                        updatePersistableUris(targetUri)
                    }
                    finish()
                }
                else -> {
                    Toast.makeText(this, getString(R.string.saf_save_sync_no_uri_selected), Toast.LENGTH_LONG).show()
                    setStorageUri(PREF_KEY_STORAGE_URI_NONE)
                    finish()
                }
            }
        }

        resultLauncher.launch(intent)
    }

    private fun setStorageUri(uri: String) {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(this).edit()
        val preferenceKey = getString(R.string.pref_key_saf_uri)
        sharedPreferences.putString(preferenceKey, uri)
        sharedPreferences.apply()
    }

    private fun updatePersistableUris(uri: Uri) {
        val contentResolver = applicationContext.contentResolver

        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

}


