package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

class ActivateSAFActivity : AppCompatActivity() {

    companion object {
        const val PREF_KEY_STORAGE_URI_NONE = ""
        private const val REQUEST_CODE_PICK_SAVEGAMEFOLDER = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activate_safactivity)

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
            e.printStackTrace()
        }
    }


    private fun getStorageUri(): String {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(this)
        val preferenceKey = getString(R.string.pref_key_saf_uri)
        val uri = sharedPreferences.getString(preferenceKey, PREF_KEY_STORAGE_URI_NONE)
        return uri ?: PREF_KEY_STORAGE_URI_NONE
    }

    private fun setStorageUri(uri: String) {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(this).edit()
        val preferenceKey = getString(R.string.pref_key_saf_uri)
        sharedPreferences.putString(preferenceKey, uri)
        sharedPreferences.apply()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_SAVEGAMEFOLDER && resultCode == Activity.RESULT_OK) {

            val currentValue: String = getStorageUri()
            val newValue = resultData?.data

            if (newValue != null && newValue.toString() != currentValue) {
                updatePersistableUrisRW(newValue)
                setStorageUri(newValue.toString())
            }
        }
        finish()
    }

    private fun updatePersistableUrisRW(uri: Uri) {

        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
}
