package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.reactivex.Single
import java.lang.Thread.sleep

class LibraryIndexMonitor(private val appContext: Context) {

    fun getSingle(): Single<Boolean> {
        val workInfos = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWork(LibraryIndexWork.UNIQUE_WORK_ID)

        return Single.create { emitter ->
            while (!workInfos.isDone) {
                sleep(1000)
            }
            val values = workInfos.get()
            emitter.onSuccess(values.any {
                it.state in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED)
            })
        }
    }

    fun getLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(LibraryIndexWork.UNIQUE_WORK_ID)

        return Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }

            isRunning
        }
    }
}
