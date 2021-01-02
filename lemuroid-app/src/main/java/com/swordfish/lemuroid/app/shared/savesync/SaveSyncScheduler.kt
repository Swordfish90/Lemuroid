package com.swordfish.lemuroid.app.shared.savesync

import android.content.Context
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager

class SaveSyncScheduler(
    private val appContext: Context,
    private val settingsManager: SettingsManager
) {

    fun cancelSaveSync() {
        SaveSyncWork.cancelAutoWork(appContext)
        SaveSyncWork.cancelManualWork(appContext)
    }

    fun scheduleSaveSyncIfNeeded(delayInMinutes: Long) {
        if (settingsManager.autoSaveSync) {
            SaveSyncWork.enqueueAutoWork(appContext, delayInMinutes)
        }
    }
}
