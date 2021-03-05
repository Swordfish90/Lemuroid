/*
 * CoreManager.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.ext.feature.core

import android.content.Context
import android.util.Log
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.swordfish.lemuroid.ext.utils.toCompletable
import com.swordfish.lemuroid.ext.utils.toSingle
import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Retrofit
import java.io.File
import java.lang.RuntimeException

class CoreManagerImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreManager {

    private val api = retrofit.create(CoreManager.CoreManagerApi::class.java)

    private fun prepareCore(context: Context, coreID: CoreID) = Single.create<String> { emitter ->
        val libraryFile = findLibrary(context, coreID)

        if (libraryFile != null && libraryFile.exists()) {
            emitter.onSuccess(libraryFile.absolutePath)
            return@create
        }

        Log.i(TAG_LOG, "Library file was not found. Retrieving core.")

        val installManager = SplitInstallManagerFactory.create(context)
        val moduleSplitName = "lemuroid_core_${coreID.coreName}"

        cancelPendingInstalls(installManager).onErrorComplete().blockingAwait()

        Log.i(TAG_LOG, "Cores is not installed")

        var currentSessionId = 0

        val request = SplitInstallRequest.newBuilder()
            .addModule(moduleSplitName)
            .build()

        val splitInstallListener = object : SplitInstallStateUpdatedListener {

            override fun onStateUpdate(state: SplitInstallSessionState) {
                Log.i(TAG_LOG, "SplitInstall update $state")
                if (state.sessionId() == currentSessionId) {
                    when (state.status()) {
                        SplitInstallSessionStatus.INSTALLED -> {
                            emitSuccess()
                        }
                        SplitInstallSessionStatus.FAILED -> {
                            emitFailure()
                        }
                        SplitInstallSessionStatus.CANCELED -> {
                            emitFailure()
                        }
                    }
                }
            }

            fun emitSuccess() {
                installManager.unregisterListener(this)

                val library = findLibrary(context, coreID)
                if (library != null) {
                    emitter.onSuccess(library.absolutePath)
                } else {
                    emitter.onError(RuntimeException("Library file not found"))
                }
            }

            fun emitFailure() {
                installManager.unregisterListener(this)
                emitter.onError(RuntimeException("Error while installing module"))
            }
        }

        installManager.registerListener(splitInstallListener)

        installManager.startInstall(request)
            .addOnSuccessListener {
                Log.i(TAG_LOG, "SplitInstall successfully initiated")
                currentSessionId = it
            }
            .addOnFailureListener {
                Log.i(TAG_LOG, "Error installing core: ${it.message}")
                emitter.onError(it)
            }
    }

    private fun cancelPendingInstalls(installManager: SplitInstallManager): Completable {
        return installManager.sessionStates.toSingle()
            .flatMapCompletable { sessionsStates ->
                Completable.concat(
                    sessionsStates.map {
                        installManager.cancelInstall(it.sessionId())
                            .toCompletable()
                            .doOnComplete { Log.i(TAG_LOG, "Stopping install for session: $it") }
                            .onErrorComplete()
                    }
                )
            }
    }

    private fun findLibrary(context: Context, coreID: CoreID): File? {
        val files = sequenceOf(
            File(context.applicationInfo.nativeLibraryDir),
            context.filesDir
        )

        return files
            .flatMap { it.walkBottomUp() }
            .firstOrNull { it.name == coreID.libretroFileName }
    }

    override fun downloadCore(
        context: Context,
        coreID: CoreID,
        assetsManager: CoreManager.AssetsManager
    ): Single<String> {
        return assetsManager.retrieveAssetsIfNeeded(api, directoriesManager)
            .andThen(prepareCore(context, coreID))
    }

    companion object {
        // Sadly dynamic features need to be tested directly on GooglePlay. Let's leave logging on.
        private const val TAG_LOG = "CoreManagerImpl"
    }
}
