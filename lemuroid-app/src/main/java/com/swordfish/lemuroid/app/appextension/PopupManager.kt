/*
 * Copyright (c) 2022 FullDive
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

package com.swordfish.lemuroid.app.appextension

import android.app.Activity
import android.content.Context
import com.swordfish.lemuroid.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PopupManager(private val context: Context) {

    private val client = OkHttpClient()
    private val popupsFlow = listOf(
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.RateUs,
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.Empty,
        StartAppDialog.InstallBrowser,
        StartAppDialog.Empty,
        StartAppDialog.Empty
    )

    val sharedPreferences by lazy { context.getPrivateSharedPreferences() }

    fun onAppStarted(activity: Activity) {
        val startCounter = sharedPreferences.getProperty(KEY_START_APP_COUNTER, 0)
        sharedPreferences.setProperty(KEY_START_APP_COUNTER, startCounter + 1)

        val rateUsDone = sharedPreferences.getProperty(KEY_RATE_US_DONE, false)
        val installBrowserDone = sharedPreferences.getProperty(KEY_INSTALL_BROWSER_DONE, false)

        if ((!rateUsDone || !installBrowserDone)) {
            when (getShowingPopup(startCounter)) {
                StartAppDialog.RateUs -> {
                    if (!rateUsDone) {
                        showRateUsDialog(activity) {
                            onRateUsPositiveClicked(activity, it)
                        }
                    }
                }

                StartAppDialog.InstallBrowser -> {
                    if (!isProVersion() && !installBrowserDone && !isBrowserInstalled()) {
                        showInstallBrowserDialog(activity) {
                            onInstallAppPositiveClicked()
                        }
                    }
                }

                else -> {
                }
            }
        }
    }

    private fun isBrowserInstalled(): Boolean {
        val app = try {
            context.packageManager.getApplicationInfo(BROWSER_PACKAGE_NAME, 0)
        } catch (e: Exception) {
            null
        }
        return app?.enabled ?: false
    }

    private fun onRateUsPositiveClicked(activity: Activity, rating: Int) {
        if (rating < SUCCESS_RATING_VALUE) {
            showRateReportDialog(activity) { message ->
                sendMessage(message)
                sharedPreferences.setProperty(KEY_RATE_US_DONE, true)
            }
        } else {
            context.openAppInGooglePlay(BuildConfig.APPLICATION_ID)
            sharedPreferences.setProperty(KEY_RATE_US_DONE, true)
        }
    }

    private fun onInstallAppPositiveClicked() {
        context.openAppInGooglePlay(BROWSER_PACKAGE_NAME)
        sharedPreferences.setProperty(KEY_INSTALL_BROWSER_DONE, true)
    }

    private fun sendMessage(message: String) {
        Thread {
            try {
                post(INBOX_URL, getJSON(message))
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
            .start()
    }

    private fun getJSON(message: String): String {
        return "{\"payload\":{\"message\":\"$message\"},\"type\":\"report-message\"}"
    }

    private fun getShowingPopup(startCounter: Int): StartAppDialog {
        return if (popupsFlow.lastIndex >= startCounter) {
            popupsFlow[startCounter]
        } else {
            popupsFlow[startCounter % popupsFlow.size]
        }
    }

    private fun showRateUsDialog(activity: Activity, positiveClickListener: (value: Int) -> Unit) {
        RateUsDialogBuilder.show(activity) { value ->
            positiveClickListener.invoke(value)
        }
    }

    private fun showRateReportDialog(activity: Activity, positiveClickListener: (message: String) -> Unit) {
        RateReportDialogBuilder.show(activity) { message ->
            positiveClickListener.invoke(message)
        }
    }

    private fun showInstallBrowserDialog(activity: Activity, positiveClickListener: () -> Unit) {
        InstallBrowserDialogBuilder.show(activity) {
            positiveClickListener.invoke()
        }
    }

    @Throws(IOException::class)
    private fun post(url: String, json: String): String {
        val body: RequestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        var result = ""
        client.newCall(request).execute().use { response ->
            result = response.body.toString()
        }
        return result
    }

    fun setProVersionPopupClosed(isClosed: Boolean) {
        sharedPreferences.setProperty(KEY_IS_PRO_POPUP_CLOSED, isClosed)
        sharedPreferences.setProperty(
            KEY_IS_PRO_POPUP_CLOSED_START_COUNTER,
            getCurrentStartCounter()
        )
    }

    fun setDiscordPopupClosed(isClosed: Boolean) {
        sharedPreferences.setProperty(KEY_IS_DISCORD_POPUP_CLOSED, isClosed)
    }

    private fun isDiscordPopupClosed(): Boolean {
        return sharedPreferences.getProperty(KEY_IS_DISCORD_POPUP_CLOSED, false)
    }

    private fun getCurrentStartCounter(): Int {
        val sharedPreferences = context.getPrivateSharedPreferences()
        return sharedPreferences.getProperty(KEY_START_APP_COUNTER, 0)
    }

    private fun isProVersionPopupClosed(): Boolean {
        return sharedPreferences.getProperty(KEY_IS_PRO_POPUP_CLOSED, false)
    }

    private fun getPromoCloseStartCounter(): Int {
        return sharedPreferences.getProperty(KEY_IS_PRO_POPUP_CLOSED_START_COUNTER, 0)
    }

    fun isProPopupVisible(): Boolean {
        val startCount = getCurrentStartCounter()
        return when {
            isProVersion() -> false
            startCount % 3 == 0 -> true
            else -> false
        }
    }

    fun isDiscordPopupVisible(): Boolean {
        val startCount = getCurrentStartCounter()
        return startCount % 2 == 1
    }

    companion object {
        const val DISCORD_INVITATION = "https://discord.gg/PZfruqZSfU"
        private const val INBOX_URL = "https://api.fdvr.co/v2/inbox"
        private const val KEY_START_APP_COUNTER = "KEY_START_APP_COUNTER"
        private const val KEY_IS_FIN_WIZE_CLOSED = "KEY_IS_FIN_WIZE_CLOSED"
        private const val KEY_RATE_US_DONE = "KEY_RATE_US_DONE"
        private const val KEY_INSTALL_BROWSER_DONE = "KEY_INSTALL_BROWSER_DONE"

        private const val KEY_IS_PRO_POPUP_CLOSED = "KEY_IS_PRO_POPUP_CLOSED"
        private const val KEY_IS_DISCORD_POPUP_CLOSED = "KEY_IS_DISCORD_POPUP_CLOSED"
        private const val KEY_IS_PRO_POPUP_CLOSED_START_COUNTER =
            "KEY_IS_PRO_POPUP_CLOSED_START_COUNTER"

        private const val KEY_IS_FIN_WIZE_CLOSED_START_COUNTER =
            "KEY_IS_FIN_WIZE_CLOSED_START_COUNTER"

        private const val BROWSER_PACKAGE_NAME = "com.fulldive.mobile"
        private const val SUCCESS_RATING_VALUE = 4
    }
}

sealed class StartAppDialog(val id: String) {
    object RateUs : StartAppDialog("RateUs")
    object InstallBrowser : StartAppDialog("InstallBrowser")
    object Empty : StartAppDialog("Empty")
}

const val FIN_WIZE_APP =
    "ai.invest.stock.market.top.finance.trade.crypto.news.chat.bot.make.money.new.etf.save"
