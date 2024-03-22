package com.swordfish.lemuroid.common.preferences

import androidx.preference.PreferenceDataStore

object DummyDataStore : PreferenceDataStore() {
    override fun putString(
        key: String?,
        value: String?,
    ) {}

    override fun putStringSet(
        key: String?,
        values: MutableSet<String>?,
    ) {}

    override fun putInt(
        key: String?,
        value: Int,
    ) {}

    override fun putLong(
        key: String?,
        value: Long,
    ) {}

    override fun putFloat(
        key: String?,
        value: Float,
    ) {}

    override fun putBoolean(
        key: String?,
        value: Boolean,
    ) {}

    override fun getString(
        key: String?,
        defValue: String?,
    ): String? = defValue

    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?,
    ): MutableSet<String> = mutableSetOf()

    override fun getInt(
        key: String?,
        defValue: Int,
    ): Int = defValue

    override fun getLong(
        key: String?,
        defValue: Long,
    ): Long = defValue

    override fun getFloat(
        key: String?,
        defValue: Float,
    ): Float = defValue

    override fun getBoolean(
        key: String?,
        defValue: Boolean,
    ): Boolean = defValue
}
