package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

class PendingOperationsMonitor(private val appContext: Context) {
    enum class Operation(val uniqueId: String, val isPeriodic: Boolean) {
        LIBRARY_INDEX(LibraryIndexScheduler.LIBRARY_INDEX_WORK_ID, false),
        CORE_UPDATE(LibraryIndexScheduler.CORE_UPDATE_WORK_ID, false),
        SAVES_SYNC_PERIODIC(SaveSyncWork.UNIQUE_PERIODIC_WORK_ID, true),
        SAVES_SYNC_ONE_SHOT(SaveSyncWork.UNIQUE_WORK_ID, false),
    }

    fun anyOperationInProgress(): Flow<Boolean> {
        return operationsInProgress(*Operation.values())
    }

    fun anySaveOperationInProgress(): Flow<Boolean> {
        return operationsInProgress(Operation.SAVES_SYNC_ONE_SHOT, Operation.SAVES_SYNC_PERIODIC)
    }

    fun anyLibraryOperationInProgress(): Flow<Boolean> {
        return operationsInProgress(Operation.LIBRARY_INDEX, Operation.CORE_UPDATE)
    }

    fun isDirectoryScanInProgress(): Flow<Boolean> {
        return operationsInProgress(Operation.LIBRARY_INDEX)
    }

    @OptIn(FlowPreview::class)
    private fun operationsInProgress(vararg operations: Operation): Flow<Boolean> {
        val operationFlows = operations.map { operationInProgress(it) }
        val result =
            combine(operationFlows) { operationInProgress ->
                operationInProgress.any { it }
            }
        return result.debounce(100)
    }

    private fun operationInProgress(operation: Operation): Flow<Boolean> {
        return WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkFlow(operation.uniqueId)
            .map { if (operation.isPeriodic) isPeriodicJobRunning(it) else isJobRunning(it) }
    }

    private fun isJobRunning(workInfos: List<WorkInfo>): Boolean {
        return workInfos
            .map { it.state }
            .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }
    }

    private fun isPeriodicJobRunning(workInfos: List<WorkInfo>): Boolean {
        return workInfos
            .map { it.state }
            .any { it in listOf(WorkInfo.State.RUNNING) }
    }
}
