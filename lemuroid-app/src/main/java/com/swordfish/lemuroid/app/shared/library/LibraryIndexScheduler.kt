package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object LibraryIndexScheduler {
    val UNIQUE_WORK_ID: String = LibraryIndexScheduler::class.java.simpleName

    fun scheduleFullSync(applicationContext: Context) {
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<LibraryIndexWork>().build()
            )
            .then(OneTimeWorkRequestBuilder<CoreUpdateWork>().build())
            .enqueue()
    }

    fun scheduleCoreUpdate(applicationContext: Context) {
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<CoreUpdateWork>().build()
            )
            .enqueue()
    }
}
