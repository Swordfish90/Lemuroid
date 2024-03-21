package com.swordfish.lemuroid.common.preferences

import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore

class SharedPreferencesDataStore(
    private val sharedPreferences: SharedPreferences,
) : PreferenceDataStore() {
    override fun putString(
        key: String?,
        value: String?,
    ) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun putStringSet(
        key: String?,
        values: MutableSet<String>?,
    ) {
        sharedPreferences.edit().putStringSet(key, values).apply()
    }

    override fun putInt(
        key: String?,
        value: Int,
    ) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun putLong(
        key: String?,
        value: Long,
    ) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun putFloat(
        key: String?,
        value: Float,
    ) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    override fun putBoolean(
        key: String?,
        value: Boolean,
    ) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getString(
        key: String?,
        defValue: String?,
    ): String? {
        return sharedPreferences.getString(key, defValue) ?: defValue
    }

    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?,
    ): MutableSet<String> {
        return sharedPreferences.getStringSet(key, defValues) ?: mutableSetOf()
    }

    override fun getInt(
        key: String?,
        defValue: Int,
    ): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    override fun getLong(
        key: String?,
        defValue: Long,
    ): Long {
        return sharedPreferences.getLong(key, defValue)
    }

    override fun getFloat(
        key: String?,
        defValue: Float,
    ): Float {
        return sharedPreferences.getFloat(key, defValue)
    }

    override fun getBoolean(
        key: String?,
        defValue: Boolean,
    ): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }
}
