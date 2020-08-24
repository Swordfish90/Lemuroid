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

package com.swordfish.lemuroid.lib.core

import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.zip.ZipInputStream

class CoreManager(private val directoriesManager: DirectoriesManager, retrofit: Retrofit) {

    private val api = retrofit.create(CoreManagerApi::class.java)

    init {
        // Force deletion of already downloaded cores.
        directoriesManager.getCoresDirectory()
    }

    fun downloadCore(gameSystem: GameSystem, assetsManager: AssetsManager): Single<String> {
        return assetsManager.retrieveAssetsIfNeeded(api, directoriesManager)
            .andThen(Single.just(gameSystem.coreFileName))
    }

    interface CoreManagerApi {

        @GET
        @Streaming
        fun downloadZip(@Url url: String): Single<Response<ZipInputStream>>
    }

    interface AssetsManager {
        fun retrieveAssetsIfNeeded(coreManagerApi: CoreManagerApi, directoriesManager: DirectoriesManager): Completable
        fun clearAssets(directoriesManager: DirectoriesManager): Completable
    }
}
