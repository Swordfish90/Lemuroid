package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager

class LibraryIndexMonitor(private val appContext: Context) {

    fun getLiveData(): LiveData<Boolean> {
        val workInfosLiveData =
                WorkManager.getInstance(appContext).getWorkInfosForUniqueWorkLiveData(LibraryIndexWork.UNIQUE_WORK_ID)

        return Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                    .map { it.state }
                    .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }
            isRunning
        }
    }
}
