package com.swordfish.lemuroid.app.shared.savesync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData

class SaveSyncMonitor(private val appContext: Context) {

    fun getLiveData(): LiveData<Boolean> {
        return CombinedLiveData(getPeriodicLiveData(), getOneTimeLiveData()) { b1, b2 ->
            (b1 ?: true) || (b2 ?: true)
        }
    }

    private fun getPeriodicLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(SaveSyncWork.UNIQUE_PERIODIC_WORK_ID)

        return Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.RUNNING) }

            isRunning
        }
    }

    private fun getOneTimeLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(SaveSyncWork.UNIQUE_WORK_ID)

        return Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING) }

            isRunning
        }
    }
}
