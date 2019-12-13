package com.swordfish.lemuroid.app.feature.settings

import android.content.Context
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable

class SettingsInteractor(private val context: Context) {

    fun changeLocalStorageFolder() {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun clearCoresCache() = Completable.fromAction {
        DirectoriesManager(context)
                .getCoresDirectory()
                .listFiles()
                ?.filterNotNull()
                ?.forEach { it.delete() }
    }
}
