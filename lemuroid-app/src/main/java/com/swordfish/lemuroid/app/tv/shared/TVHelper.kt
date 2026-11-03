package com.swordfish.lemuroid.app.tv.shared

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object TVHelper {
    fun isSAFSupported(context: Context): Boolean {
        val packageManager = context.packageManager

        val isStandardHardware =
            listOf(
                !packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK),
                !packageManager.hasSystemFeature("android.hardware.type.television"),
                !packageManager.hasSystemFeature("android.hardware.type.watch"),
                !packageManager.hasSystemFeature("android.hardware.type.automotive"),
            ).all { it }

        if (!isStandardHardware) {
            return false
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        return intent.resolveActivity(packageManager) != null
    }

    fun isTV(context: Context): Boolean {
        val packageManager = context.packageManager
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
            packageManager.hasSystemFeature("android.hardware.type.television")
    }
}
