package com.swordfish.lemuroid.ext.feature.core

import android.content.Context
import android.util.Log
import com.google.android.play.core.ktx.hasTerminalStatus
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.swordfish.lemuroid.ext.utils.toCompletable
import com.swordfish.lemuroid.ext.utils.toSingle
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreUpdater {

    private val api = retrofit.create(CoreUpdater.CoreManagerApi::class.java)

    override fun downloadCores(context: Context, coreIDs: List<CoreID>): Completable {
        val splitInstallManager = SplitInstallManagerFactory.create(context)

        return cancelPendingInstalls(splitInstallManager)
            .andThen(installCores(splitInstallManager, coreIDs))
            .andThen(installAssets(coreIDs))
            .doAfterTerminate { log("Terminating downloadCores") }
    }

    private fun buildInstallRequest(
        splitInstallManager: SplitInstallManager,
        coreIDs: List<CoreID>
    ) = Maybe.fromCallable<SplitInstallRequest> {
        val requiredCores = coreIDs
            .map { computePlayModuleName(it) }
            .filter { it !in splitInstallManager.installedModules }

        if (requiredCores.isEmpty()) {
            return@fromCallable null
        }

        log("Starting install for the following cores: $requiredCores")

        val result = SplitInstallRequest.newBuilder()
        requiredCores.forEach { result.addModule(it) }

        result.build()
    }

    private fun computePlayModuleName(it: CoreID) = "lemuroid_core_${it.coreName}"

    private fun startInstallRequest(
        request: SplitInstallRequest,
        splitInstallManager: SplitInstallManager
    ): Single<Int> {
        return splitInstallManager.startInstall(request)
            .toSingle()
            .doOnSuccess { log("Session started with id: $it") }
            .map { it }
    }

    private fun waitForCompletion(sessionId: Int, splitInstallManager: SplitInstallManager): Completable {
        val eventsSubject = PublishSubject.create<SplitInstallSessionState>()

        val listener = SplitInstallStateUpdatedListener {
            eventsSubject.onNext(it)
        }

        return eventsSubject
            .doOnNext { log("Session status updated to $it") }
            .filter { it.sessionId() == sessionId }
            .takeUntil { it.hasTerminalStatus }
            .doAfterTerminate { log("Terminating monitor install for session: $sessionId") }
            .ignoreElements()
            .doOnSubscribe { splitInstallManager.registerListener(listener) }
            .doAfterTerminate { splitInstallManager.unregisterListener(listener) }
    }

    private fun installCores(splitInstallManager: SplitInstallManager, coreIDs: List<CoreID>): Completable {
        return buildInstallRequest(splitInstallManager, coreIDs)
            .flatMapSingle { startInstallRequest(it, splitInstallManager) }
            .flatMapCompletable { waitForCompletion(it, splitInstallManager) }
    }

    private fun installAssets(coreIDs: List<CoreID>): Completable {
        return Observable.fromIterable(coreIDs)
            .map { CoreID.getAssetManager(it) }
            .flatMapCompletable { it.retrieveAssetsIfNeeded(api, directoriesManager) }
    }

    private fun cancelPendingInstalls(installManager: SplitInstallManager): Completable {
        return installManager.sessionStates.toSingle()
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMapCompletable {
                installManager.cancelInstall(it.sessionId())
                    .toCompletable()
                    .andThen(waitForCompletion(it.sessionId(), installManager))
                    .doOnComplete { log("Stopped install for session: $it") }
                    .onErrorComplete()
            }
            .doAfterTerminate { log("Terminating cancelPendingInstalls") }
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
