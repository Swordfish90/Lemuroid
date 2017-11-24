/*
 * DriveFactory.kt
 *
 * Copyright (C) 2017 Odyssey Project
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

package com.codebutler.odyssey.storage.gdrive

import android.content.Context
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive

class DriveFactory(private val context: Context) {
    fun create(): Optional<Drive> {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return None

        val scopeNames = GDriveStorageProvider.SCOPES.map { scope -> scope.toString() }

        val credential = GoogleAccountCredential.usingOAuth2(context, scopeNames)
        credential.selectedAccount = googleAccount.account

        val httpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = AndroidJsonFactory.getDefaultInstance()

        return Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(BuildConfig.APPLICATION_ID)
                .build()
                .toOptional()
    }
}
