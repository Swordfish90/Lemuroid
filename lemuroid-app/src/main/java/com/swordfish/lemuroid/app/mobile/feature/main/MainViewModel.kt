package com.swordfish.lemuroid.app.mobile.feature.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager

class MainViewModel(appContext: Context, private val saveSyncManager: SaveSyncManager) : ViewModel() {
    class Factory(
        private val appContext: Context,
        private val saveSyncManager: SaveSyncManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(appContext, saveSyncManager) as T
        }
    }

    data class UiState(
        val operationInProgress: Boolean = false,
        val saveSyncEnabled: Boolean = false,
    )

    private val saveSyncEnabledLiveData = MutableLiveData(false)
    private val operationInProgressLiveData =
        PendingOperationsMonitor(appContext)
            .anyOperationInProgress()

    val state =
        CombinedLiveData(
            saveSyncEnabledLiveData,
            operationInProgressLiveData,
            this::buildState,
        )

    fun update() {
        val current = saveSyncManager.isSupported() && saveSyncManager.isConfigured()
        saveSyncEnabledLiveData.postValue(current)
    }

    private fun buildState(
        saveSyncEnabled: Boolean,
        operationInProgress: Boolean,
    ): UiState {
        return UiState(operationInProgress, saveSyncEnabled)
    }
}
