package com.swordfish.lemuroid.app.feature.settings

import android.content.Context
import com.swordfish.lemuroid.app.feature.settings.StorageFrameworkPickerLauncher

class SettingsInteractor(private val context: Context) {

    fun changeLocalStorageFolder() {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }
}
