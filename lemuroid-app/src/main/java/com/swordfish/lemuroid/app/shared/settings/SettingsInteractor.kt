package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.app.shared.library.LibraryIndexWork
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork

class SettingsInteractor(
    private val context: Context
) {
    fun changeLocalStorageFolder() {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun resetAllSettings() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
        LibraryIndexWork.enqueueUniqueWork(context.applicationContext)
        CacheCleanerWork.enqueueCleanCacheAll(context.applicationContext)
    }
}
