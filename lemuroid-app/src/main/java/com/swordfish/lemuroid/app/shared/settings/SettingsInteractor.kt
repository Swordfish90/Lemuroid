package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.app.shared.library.LibraryIndexWork
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork

class SettingsInteractor(
    private val context: Context,
    private val directoriesManager: DirectoriesManager
) {
    fun changeLocalStorageFolder() {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun resetAllSettings() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
        LibraryIndexWork.enqueueUniqueWork(context.applicationContext)
        CacheCleanerWork.enqueueCleanCacheAll(context.applicationContext)
        deleteDownloadedCores()
    }

    private fun deleteDownloadedCores() {
        directoriesManager.getCoresDirectory()
            .listFiles()
            ?.forEach { runCatching { it.deleteRecursively() } }
    }
}
