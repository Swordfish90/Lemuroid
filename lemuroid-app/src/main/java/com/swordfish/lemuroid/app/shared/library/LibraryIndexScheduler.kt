package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object LibraryIndexScheduler {
    val CORE_UPDATE_WORK_ID: String = CoreUpdateWork::class.java.simpleName
    val LIBRARY_INDEX_WORK_ID: String = LibraryIndexWork::class.java.simpleName

    fun scheduleLibrarySync(applicationContext: Context) {
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                LIBRARY_INDEX_WORK_ID,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<LibraryIndexWork>().build(),
            )
            .enqueue()
    }

    fun scheduleCoreUpdate(applicationContext: Context) {
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                CORE_UPDATE_WORK_ID,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<CoreUpdateWork>().build(),
            )
            .enqueue()
    }

    fun cancelLibrarySync(applicationContext: Context) {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(LIBRARY_INDEX_WORK_ID)
    }

    fun cancelCoreUpdate(applicationContext: Context) {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(CORE_UPDATE_WORK_ID)
    }
}
