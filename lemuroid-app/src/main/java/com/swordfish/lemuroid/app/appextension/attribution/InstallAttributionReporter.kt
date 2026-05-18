/*
 * Copyright (c) 2024 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension.attribution

import android.content.Context
import android.net.Uri
import android.provider.Settings
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.getPrivateSharedPreferences
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object InstallAttributionReporter {

    private const val PREF_ATTRIBUTION_REPORTED = "attribution_reported"

    fun reportIfNeeded(context: Context, scope: CoroutineScope) {
        if (context.getPrivateSharedPreferences().getBoolean(PREF_ATTRIBUTION_REPORTED, false)) return
        if (BuildConfig.ONE_EMULATOR_ATTRIBUTION_SECRET.isEmpty()) return

        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        val referrer = referrerClient.installReferrer.installReferrer
                        val params = Uri.parse("?$referrer")
                        val ref = params.getQueryParameter("ref")
                        val challengeId = params.getQueryParameter("challenge")
                        if (!ref.isNullOrEmpty() && !challengeId.isNullOrEmpty()) {
                            scope.safeLaunch(Dispatchers.IO) {
                                reportAttribution(context, ref, challengeId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Install referrer read failed")
                } finally {
                    referrerClient.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }

    private fun reportAttribution(context: Context, ref: String, challengeId: String) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: return

        val secret = BuildConfig.ONE_EMULATOR_ATTRIBUTION_SECRET
        val hmac = hmacSha256(secret, "$ref:$challengeId:$deviceId")

        val body = JSONObject().apply {
            put("ref", ref)
            put("challengeId", challengeId)
            put("platform", "one_emulator")
            put("deviceId", deviceId)
            put("hmac", hmac)
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(FulldiveConfigs.ROOMCORD_ATTRIBUTION_URL)
            .post(body)
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                context.getPrivateSharedPreferences()
                    .edit()
                    .putBoolean(PREF_ATTRIBUTION_REPORTED, true)
                    .apply()
                Timber.i("Install attribution reported: ref=$ref, challengeId=$challengeId")
            } else {
                Timber.w("Install attribution failed: HTTP ${response.code}")
            }
        }
    }

    private fun hmacSha256(secret: String, message: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(message.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
