package com.swordfish.lemuroid.app.tv.shared

import android.content.Context
import android.os.Build
import android.os.Environment

object TVHelper {
    fun isSAFSupported(context: Context): Boolean {
        val packageManager = context.packageManager

        val isStandardHardware =
            !packageManager.hasSystemFeature("android.hardware.type.television") and
            !packageManager.hasSystemFeature("android.hardware.type.watch") and
            !packageManager.hasSystemFeature("android.hardware.type.automotive")

        val isNotLegacyStorage =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()

        return isStandardHardware || isNotLegacyStorage
    }

    fun isTV(context: Context): Boolean {
        val packageManager = context.packageManager
        return packageManager.hasSystemFeature("android.hardware.type.television")
    }
}
