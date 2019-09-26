package com.codebutler.retrograde.app.feature.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.codebutler.retrograde.lib.library.LibraryIndexWork

class MainViewModel(appContext: Context) : ViewModel() {

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(appContext) as T
        }
    }

    val indexingInProgress = WorkManager.getInstance(appContext).getWorkInfosForUniqueWorkLiveData(LibraryIndexWork.UNIQUE_WORK_ID)
}
