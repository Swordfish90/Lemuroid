package com.swordfish.lemuroid.app.tv.shared

import android.content.Context
import android.content.pm.PackageManager

object TVHelper {
    fun isSAFSupported(context: Context): Boolean {
        val pm: PackageManager = context.packageManager
        return !(
            pm.hasSystemFeature("android.hardware.type.television") or
            pm.hasSystemFeature("android.hardware.type.watch") or
            pm.hasSystemFeature("android.hardware.type.automotive")
        )
    }
}
