/*
 * DriveFactory.kt
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

package com.swordfish.lemuroid.ext.feature.savesync

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

// RestrictedApi warning is a known issue:
// https://developers.google.com/android/guides/releases#march_20_2018_-_version_1200
@SuppressLint("RestrictedApi")
class DriveFactory(private val context: Context) {
    fun create(): Drive? {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return null

        val scopeNames = listOf(Scope(DriveScopes.DRIVE_APPDATA).toString())

        val credential = GoogleAccountCredential.usingOAuth2(context, scopeNames)
        credential.selectedAccount = googleAccount.account

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        return Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("com.swordfish.lemuroid")
            .build()
    }
}
