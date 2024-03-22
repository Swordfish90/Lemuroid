package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.text.format.Formatter
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner

object AdvancedSettingsPreferences {
    fun updateCachePreferences(preferenceScreen: PreferenceScreen) {
        val cacheKey = preferenceScreen.context.getString(R.string.pref_key_max_cache_size)
        preferenceScreen.findPreference<ListPreference>(cacheKey)?.apply {
            val supportedCacheValues = CacheCleaner.getSupportedCacheLimits()

            entries =
                supportedCacheValues
                    .map { getSizeLabel(preferenceScreen.context, it) }
                    .toTypedArray()

            entryValues =
                supportedCacheValues
                    .map { it.toString() }
                    .toTypedArray()

            if (value == null) {
                setValueIndex(supportedCacheValues.indexOf(CacheCleaner.getDefaultCacheLimit()))
            }

            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }

    private fun getSizeLabel(
        appContext: Context,
        size: Long,
    ): String {
        return Formatter.formatShortFileSize(appContext, size)
    }
}
