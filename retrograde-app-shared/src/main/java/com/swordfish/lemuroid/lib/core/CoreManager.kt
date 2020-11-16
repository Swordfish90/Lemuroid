package com.swordfish.lemuroid.lib.core

import android.content.Context
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.InputStream
import java.util.zip.ZipInputStream

interface CoreManager {

    fun downloadCore(context: Context, coreID: CoreID, assetsManager: AssetsManager): Single<String>

    interface CoreManagerApi {
        @GET
        @Streaming
        fun downloadFile(@Url url: String): Single<Response<InputStream>>

        @GET
        @Streaming
        fun downloadZip(@Url url: String): Single<Response<ZipInputStream>>
    }

    interface AssetsManager {
        fun retrieveAssetsIfNeeded(coreManagerApi: CoreManagerApi, directoriesManager: DirectoriesManager): Completable
        fun clearAssets(directoriesManager: DirectoriesManager): Completable
    }
}
