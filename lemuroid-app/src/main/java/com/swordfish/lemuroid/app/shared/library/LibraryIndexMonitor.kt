package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.swordfish.lemuroid.app.utils.livedata.map
import com.swordfish.lemuroid.app.utils.livedata.throttle
import com.swordfish.lemuroid.app.utils.livedata.combineLatest

class LibraryIndexMonitor(private val appContext: Context) {

    fun getLiveData(): LiveData<Boolean> {
        return libraryIndexLiveData()
            .combineLatest(coreUpdateLiveData()) { b1, b2 -> b1 || b2 }
            .throttle(200)
    }

    private fun coreUpdateLiveData(): LiveData<Boolean> {
        return WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(LibraryIndexScheduler.CORE_UPDATE_WORK_ID)
            .map { isRunning(it) }
    }

    private fun libraryIndexLiveData(): LiveData<Boolean> {
        return WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(LibraryIndexScheduler.LIBRARY_INDEX_WORK_ID)
            .map { isRunning(it) }
    }

    private fun isRunning(workInfos: List<WorkInfo>): Boolean {
        return workInfos
            .map { it.state }
            .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }
    }
}
