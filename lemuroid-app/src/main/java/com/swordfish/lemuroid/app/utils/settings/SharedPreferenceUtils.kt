package com.swordfish.lemuroid.app.utils.settings

import android.content.SharedPreferences

fun SharedPreferences.safeGetInt(
    key: String,
    defValue: Int,
): Int {
    val result = runCatching { getInt(key, defValue) }
    return result.getOrDefault(defValue)
}

fun SharedPreferences.safeGetString(
    key: String,
    defValue: String?,
): String? {
    val result = runCatching { getString(key, defValue) }
    return result.getOrDefault(defValue)
}

fun SharedPreferences.safeGetBoolean(
    key: String,
    defValue: Boolean,
): Boolean {
    val result = runCatching { getBoolean(key, defValue) }
    return result.getOrDefault(defValue)
}

fun SharedPreferences.safeGetStringSet(
    key: String,
    defValue: Set<String>?,
): Set<String>? {
    val result = runCatching { getStringSet(key, defValue) }
    return result.getOrDefault(defValue)
}
