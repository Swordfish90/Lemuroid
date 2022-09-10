package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork

class SettingsInteractor(
    private val context: Context,
    private val directoriesManager: DirectoriesManager
) {
    fun changeLocalStorageFolder() {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun changeSavegameFolder() {
        SAFSavePickerLauncher.pickSavegameFolder(context)
    }

    fun resetAllSettings() {
        SharedPreferencesHelper.getLegacySharedPreferences(context).edit().clear().apply()
        SharedPreferencesHelper.getSharedPreferences(context).edit().clear().apply()
        LibraryIndexScheduler.scheduleFullSync(context.applicationContext)
        CacheCleanerWork.enqueueCleanCacheAll(context.applicationContext)
        deleteDownloadedCores()
    }

    private fun deleteDownloadedCores() {
        directoriesManager.getCoresDirectory()
            .listFiles()
            ?.forEach { runCatching { it.deleteRecursively() } }
    }
}
