package com.swordfish.lemuroid.app.tv.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncMonitor
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData

class MainTVViewModel(appContext: Context) : ViewModel() {

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainTVViewModel(appContext) as T
        }
    }

    private val indexingInProgress = LibraryIndexMonitor(appContext).getLiveData()
    private val saveSyncInProgress = SaveSyncMonitor(appContext).getLiveData()
    val inProgress = CombinedLiveData(indexingInProgress, saveSyncInProgress) { a, b -> a || b }
}
