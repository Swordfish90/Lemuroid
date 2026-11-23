package com.swordfish.lemuroid.app.tv.shared

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment

object TVHelper {
    private const val TV_FRAMEWORK_STUBS_PACKAGE = "com.android.tv.frameworkpackagestubs"

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

        return (isStandardHardware || isNotLegacyStorage) && hasDocumentTreePicker(context)
    }

    // Many Android TV builds ship a stub activity that claims ACTION_OPEN_DOCUMENT_TREE but does nothing, so we
    // only trust activities that don't come from the framework stubs package.
    private fun hasDocumentTreePicker(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .any { it.activityInfo?.packageName != TV_FRAMEWORK_STUBS_PACKAGE }
    }

    fun isTV(context: Context): Boolean {
        val packageManager = context.packageManager
        return packageManager.hasSystemFeature("android.hardware.type.television")
    }

    // Browsing arbitrary folders with the built-in picker needs "All files access" on Android 11+.
    fun hasAllFilesAccess(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
    }

    // Not every flavor declares MANAGE_EXTERNAL_STORAGE (it is restricted on Google Play).
    fun isAllFilesAccessDeclared(context: Context): Boolean {
        return runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                ?.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE) == true
        }.getOrDefault(false)
    }
}
