package com.swordfish.lemuroid.ext.feature.core

import android.content.Context
import android.util.Log
import com.google.android.play.core.ktx.hasTerminalStatus
import com.google.android.play.core.ktx.sessionId
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit

class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreUpdater {

    private val api = retrofit.create(CoreUpdater.CoreManagerApi::class.java)

    override suspend fun downloadCores(context: Context, coreIDs: List<CoreID>) {
        val splitInstallManager = SplitInstallManagerFactory.create(context)

        cancelPendingInstalls(splitInstallManager)
        installCores(splitInstallManager, coreIDs)
        installAssets(context, coreIDs)

        log("downloadCores has terminated")
    }

    private fun buildInstallRequest(
        splitInstallManager: SplitInstallManager,
        coreIDs: List<CoreID>
    ): SplitInstallRequest? {
        val requiredCores = coreIDs
            .map { computePlayModuleName(it) }
            .filter { it !in splitInstallManager.installedModules }

        if (requiredCores.isEmpty()) {
            return null
        }

        log("Starting install for the following cores: $requiredCores")

        val result = SplitInstallRequest.newBuilder()
        requiredCores.forEach { result.addModule(it) }

        return result.build()
    }

    private fun computePlayModuleName(it: CoreID) = "lemuroid_core_${it.coreName}"

    private suspend fun startInstallRequest(
        request: SplitInstallRequest,
        splitInstallManager: SplitInstallManager
    ): Int {
        val sessionId = splitInstallManager.startInstall(request).await()
        log("Session started with id: $sessionId")
        return sessionId
    }

    private suspend fun waitForCompletion(
        sessionId: Int,
        splitInstallManager: SplitInstallManager
    ) {
        val eventsSubject = MutableSharedFlow<SplitInstallSessionState>(1)

        val listener = SplitInstallStateUpdatedListener {
            eventsSubject.tryEmit(it)
        }

        eventsSubject
            .onEach { log("Session status updated to $it") }
            .filter { it.sessionId() == sessionId }
            .takeWhile { !it.hasTerminalStatus }
            .onStart { splitInstallManager.registerListener(listener) }
            .catch { log("Error while waitingForCompletion $it") }
            .onCompletion { splitInstallManager.unregisterListener(listener) }
            .onCompletion { log("Terminating monitor install for session: $sessionId") }
            .collect()
    }

    private suspend fun installCores(
        splitInstallManager: SplitInstallManager,
        coreIDs: List<CoreID>
    ) {
        val installRequest = buildInstallRequest(splitInstallManager, coreIDs) ?: return
        val sessionId = startInstallRequest(installRequest, splitInstallManager)
        waitForCompletion(sessionId, splitInstallManager)
    }

    private suspend fun installAssets(context: Context, coreIDs: List<CoreID>) {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context.applicationContext)
        coreIDs.asFlow()
            .map { CoreID.getAssetManager(it) }
            .onEach { it.retrieveAssetsIfNeeded(api, directoriesManager, sharedPreferences) }
            .collect()
    }

    private suspend fun cancelPendingInstalls(installManager: SplitInstallManager) {
        val sessionStates = installManager.sessionStates.await()
        sessionStates.asFlow()
            .onEach { installManager.cancelInstall(it.sessionId).await() }
            .onEach { log("Terminated install for session: ${it.sessionId}") }
            .collect()

        log("Terminating cancelPendingInstalls")
    }

    private fun log(message: String) {
        if (VERBOSE) {
            Log.i(TAG_LOG, message)
        }
    }

    companion object {
        // Sadly dynamic features need to be tested directly on GooglePlay. Let's leave logging on.
        private const val TAG_LOG = "CoreManagerImpl"
        private const val VERBOSE = true
    }
}
