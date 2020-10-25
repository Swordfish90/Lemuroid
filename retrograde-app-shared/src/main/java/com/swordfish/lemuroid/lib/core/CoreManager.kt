package com.swordfish.lemuroid.lib.core

import android.content.Context
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.zip.ZipInputStream

interface CoreManager {

    fun downloadCore(context: Context, gameSystem: GameSystem, assetsManager: AssetsManager): Single<String>

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
