package com.swordfish.lemuroid.app.tv.shared

import android.content.Context
import android.os.Build
import android.os.Environment

object TVHelper {
    fun isSAFSupported(context: Context): Boolean {
        val packageManager = context.packageManager

        // Devices that are known to have limited or no SAF support
        val isLimitedDevice = packageManager.hasSystemFeature("android.hardware.type.television") ||
                packageManager.hasSystemFeature("android.hardware.type.watch") ||
                packageManager.hasSystemFeature("android.hardware.type.automotive")

        // Scoped storage check (relevant only for Android 10+)
        val isScopedStorageRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !Environment.isExternalStorageLegacy()

        // SAF is supported if it's not a limited device and meets storage requirements
        return !isLimitedDevice && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT || isScopedStorageRequired)
    }

    fun isTV(context: Context): Boolean {
        val packageManager = context.packageManager
        return packageManager.hasSystemFeature("android.hardware.type.television")
    }
}
