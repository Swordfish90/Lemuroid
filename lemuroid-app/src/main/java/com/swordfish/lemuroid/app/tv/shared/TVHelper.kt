package com.swordfish.lemuroid.app.tv.shared

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment

object TVHelper {
    fun isSAFSupported(context: Context): Boolean {
        val packageManager = context.packageManager

        val isStandardHardware =
            listOf(
                !packageManager.hasSystemFeature("android.hardware.type.television"),
                !packageManager.hasSystemFeature("android.hardware.type.watch"),
                !packageManager.hasSystemFeature("android.hardware.type.automotive"),
            ).all { it }

        val isNotLegacyStorage =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()

        if (!isStandardHardware && !isNotLegacyStorage) {
            return false;
        }

        // In Firestick ACTION_OPEN_DOCUMENT_TREE causes ActivityNotFoundException if launched
        val resolveInfo = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).resolveActivity(packageManager)
        return resolveInfo != null
    }

    fun isTV(context: Context): Boolean {
        val packageManager = context.packageManager
        return packageManager.hasSystemFeature("android.hardware.type.television")
    }
}
