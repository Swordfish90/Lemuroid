package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.swordfish.lemuroid.ext.R
import timber.log.Timber

class ActivateGoogleDriveActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiAvailability = GoogleApiAvailability.getInstance()
        val errorCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (errorCode == ConnectionResult.SUCCESS) {
            authenticateGoogle()
        } else {
            val message = getString(R.string.gdrive_missing_play_services)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GOOGLE_SIGN_IN) {
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = completedTask.getResult(ApiException::class.java)
                val message = getString(R.string.gdrive_sign_in_success, account?.email)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: ApiException) {
                val message =
                    getString(
                        R.string.gdrive_sign_in_failed,
                        e.message,
                        e.statusCode.toString(),
                    )
                Timber.e(e, message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun authenticateGoogle() {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        val scope = Scope(DriveScopes.DRIVE_APPDATA)

        if (!GoogleSignIn.hasPermissions(lastSignedInAccount, scope)) {
            val signInClient = googleSignInClient()
            startActivityForResult(signInClient.signInIntent, REQUEST_GOOGLE_SIGN_IN)
        } else {
            disableGoogleDriveIntegration()
            finish()
        }
    }

    private fun disableGoogleDriveIntegration() {
        val signInClient = googleSignInClient()
        signInClient.signOut().addOnSuccessListener {
            Toast.makeText(this, R.string.gdrive_sign_out_success, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun googleSignInClient(): GoogleSignInClient {
        val signInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .build()
        return GoogleSignIn.getClient(this, signInOptions)
    }

    companion object {
        const val REQUEST_GOOGLE_SIGN_IN = 45
    }
}
