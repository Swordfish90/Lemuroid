/*
 * GDrivePreferencesFragment.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.storage.gdrive

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v17.preference.LeanbackPreferenceFragment
import android.support.v7.preference.Preference
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import timber.log.Timber

// RestrictedApi warning is a known issue:
// https://developers.google.com/android/guides/releases#march_20_2018_-_version_1200
@SuppressLint("RestrictedApi")
class GDrivePreferenceFragment : LeanbackPreferenceFragment() {

    companion object {
        const val PREFS_NAME = "gdrive"
        const val PREF_KEY_FOLDER_ID = "folder_id"

        private const val REQUEST_GOOGLE_SIGNIN = 10001
        private const val REQUEST_SELECT_FOLDER = 10002
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PREFS_NAME
        addPreferencesFromResource(R.xml.gdrive_prefs)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == getString(R.string.gdrive_pref_key_configure)) {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val errorCode = apiAvailability.isGooglePlayServicesAvailable(activity)
            if (errorCode == ConnectionResult.SUCCESS) {
                authenticateGoogle()
            } else {
                apiAvailability.showErrorDialogFragment(activity, errorCode, 0)
            }
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_SIGNIN -> {
                val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = completedTask.getResult(ApiException::class.java)
                    val message = getString(R.string.gdrive_sign_in_success, account.email)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    authenticateGoogleComplete()
                } catch (e: ApiException) {
                    val message = getString(R.string.gdrive_sign_in_failed, e.message, e.statusCode.toString())
                    Timber.e(e, message)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_SELECT_FOLDER -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (resultCode == RESULT_OK && data != null) {
                    val folderId = data.getStringExtra(GDriveBrowseActivity.BUNDLE_FOLDER_ID)
                    prefs.edit().putString(PREF_KEY_FOLDER_ID, folderId).apply()
                } else {
                    prefs.edit().remove(PREF_KEY_FOLDER_ID).apply()
                }
            }
        }
    }

    private fun authenticateGoogle() {
        val scopesArray = GDriveStorageProvider.SCOPES.toTypedArray()
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), *scopesArray)) {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestId()
                    .requestEmail()
                    .requestScopes(scopesArray.first(), *scopesArray.sliceArray(1..scopesArray.lastIndex))
                    .build()
            val signInClient = GoogleSignIn.getClient(context, signInOptions)
            startActivityForResult(signInClient.signInIntent, REQUEST_GOOGLE_SIGNIN)
        } else {
            authenticateGoogleComplete()
        }
    }

    private fun authenticateGoogleComplete() {
        startActivityForResult(Intent(context, GDriveBrowseActivity::class.java), REQUEST_SELECT_FOLDER)
    }
}
