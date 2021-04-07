package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.swordfish.lemuroid.app.utils.livedata.ThrottledLiveData

class LibraryIndexMonitor(private val appContext: Context) {

    fun getLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(LibraryIndexScheduler.UNIQUE_WORK_ID)

        val result = Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }

            isRunning
        }

        return ThrottledLiveData(result, 200)
    }
}
