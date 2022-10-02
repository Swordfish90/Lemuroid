package com.swordfish.lemuroid.ext.feature.core

import android.content.Context
import android.util.Log
import com.google.android.play.core.ktx.hasTerminalStatus
import com.google.android.play.core.ktx.requestCancelInstall
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.ktx.requestSessionStates
import com.google.android.play.core.ktx.sessionId
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import retrofit2.Retrofit

class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreUpdater {

    private val api = retrofit.create(CoreUpdater.CoreManagerApi::class.java)

    override suspend fun downloadCores(context: Context, coreIDs: List<CoreID>) {
        val splitInstallManager = SplitInstallManagerFactory.create(context)

        try {
            cancelPendingInstalls(splitInstallManager)
        } catch (e: Throwable) {
            log("Error while canceling pending installs: ${e.message}")
        }

        try {
            installCores(splitInstallManager, coreIDs)
        } catch (e: Throwable) {
            log("Error while installing cores: ${e.message}")
        }

        try {
            installAssets(context, coreIDs)
        } catch (e: Throwable) {
            log("Error while installing assets: ${e.message}")
        }

        log("downloadCores has terminated")
    }

    private fun computePlayModuleName(it: CoreID) = "lemuroid_core_${it.coreName}"

    private suspend fun waitForCompletion(sessionId: Int, installManager: SplitInstallManager) {
        installManager.requestProgressFlow()
            .filter { it.sessionId() == sessionId }
            .onEach { log("Session status for id $sessionId updated to $it") }
            .takeWhile { !it.hasTerminalStatus }
            .catch { log("Error while waitingForCompletion $it") }
            .onCompletion { log("Terminating waitingForCompletion for session: $sessionId") }
            .collect()
    }

    private suspend fun installCores(installManager: SplitInstallManager, coreIDs: List<CoreID>) {
        val modulesToInstall = coreIDs
            .map { computePlayModuleName(it) }
            .filter { it !in installManager.installedModules }

        if (modulesToInstall.isEmpty()) {
            return
        }

        log("Starting install for the following modules: $modulesToInstall")
        val sessionId = installManager.requestInstall(modulesToInstall)
        waitForCompletion(sessionId, installManager)
    }

    private suspend fun installAssets(context: Context, coreIDs: List<CoreID>) {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context.applicationContext)
        coreIDs.asFlow()
            .map { CoreID.getAssetManager(it) }
            .onEach { it.retrieveAssetsIfNeeded(api, directoriesManager, sharedPreferences) }
            .collect()
    }

    private suspend fun cancelPendingInstalls(installManager: SplitInstallManager) {
        flow { emitAll(installManager.requestSessionStates().asFlow()) }
            .filter { !it.hasTerminalStatus }
            .onEach { cancelPendingInstall(installManager, it.sessionId) }
            .catch { log("Failed to cancel pending installs: ${it.message}") }
            .collect()

        log("Terminating cancelPendingInstalls")
    }

    private suspend fun cancelPendingInstall(installManager: SplitInstallManager, sessionId: Int) {
        try {
            installManager.requestCancelInstall(sessionId)
            waitForCompletion(sessionId, installManager)
        } catch (e: Throwable) {
            log("Failed to cancel pending install for session: $sessionId")
        }
        log("Terminated cancel for session: $sessionId")
    }

    private fun log(message: String) {
        if (VERBOSE) {
            Log.w(TAG_LOG, message)
        }
    }

    companion object {
        // Sadly dynamic features need to be tested directly on GooglePlay. Let's leave logging on.
        private const val TAG_LOG = "CoreUpdaterImpl"
        private const val VERBOSE = true
    }
}
