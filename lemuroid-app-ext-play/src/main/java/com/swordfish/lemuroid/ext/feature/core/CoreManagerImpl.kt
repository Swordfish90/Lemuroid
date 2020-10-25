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
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.swordfish.lemuroid.lib.BuildConfig
import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Single
import retrofit2.Retrofit

class CoreManagerImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreManager {

    private val api = retrofit.create(CoreManager.CoreManagerApi::class.java)

    init {
        // Force deletion of already downloaded cores.
        directoriesManager.getCoresDirectory()
    }

    fun prepareCore(context: Context, gameSystem: GameSystem) = Single.create<String> { emitter ->
        val installManager = SplitInstallManagerFactory.create(context)
        val moduleSplitName = "lemuroid_core_${gameSystem.coreName}"
        val coreResult = context.applicationInfo.nativeLibraryDir + "/" + gameSystem.coreFileName
        if (installManager.installedModules.contains(moduleSplitName) || BuildConfig.DEBUG) {
            emitter.onSuccess(coreResult)
            return@create
        }

        val request = SplitInstallRequest.newBuilder()
            .addModule(moduleSplitName)
            .build()

        installManager.startInstall(request)
            .addOnSuccessListener { emitter.onSuccess(coreResult) }
            .addOnFailureListener {
                Log.e("FILIPPO", "Error installing core: ${it.message}", it)
                emitter.onError(it)
            }
    }

    override fun downloadCore(
        context: Context,
        gameSystem: GameSystem,
        assetsManager: CoreManager.AssetsManager
    ): Single<String> {
        return assetsManager.retrieveAssetsIfNeeded(api, directoriesManager)
            .andThen(prepareCore(context, gameSystem))
    }
}
