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
import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Single
import retrofit2.Retrofit
import java.io.File
import java.io.FileNotFoundException

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
        val coreResult = context.applicationInfo.nativeLibraryDir + "/" + gameSystem.coreFileName
        if (File(coreResult).exists()) {
            emitter.onSuccess(coreResult)
        } else {
            emitter.onError(FileNotFoundException("Core is missing: ${gameSystem.coreFileName}"))
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
