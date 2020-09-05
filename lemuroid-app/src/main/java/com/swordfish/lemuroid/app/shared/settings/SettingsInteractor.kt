package com.swordfish.lemuroid.app.shared.settings

import android.content.Context

class SettingsInteractor(private val context: Context) {
    fun changeLocalStorageFolder() {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }
}
