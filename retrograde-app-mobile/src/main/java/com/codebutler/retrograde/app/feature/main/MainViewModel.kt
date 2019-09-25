package com.codebutler.retrograde.app.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.codebutler.retrograde.lib.library.LibraryIndexWork

class MainViewModel : ViewModel() {

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel() as T
        }
    }

    val indexingInProgress = WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(LibraryIndexWork.UNIQUE_WORK_ID)
}
