package com.swordfish.lemuroid.ext.feature.donate

import android.app.Activity
import android.content.Context

class IAPHandler(private val applicationContext: Context) {

    companion object {
        const val IS_SUPPORTED = false

        fun launchDonateScreen(activity: Activity) { }

        fun launchTVDonateScreen(activity: Activity) { }
    }
}
