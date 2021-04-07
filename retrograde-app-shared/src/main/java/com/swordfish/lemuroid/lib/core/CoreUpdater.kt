package com.swordfish.lemuroid.lib.core

import android.content.Context
import com.swordfish.lemuroid.lib.library.CoreID
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.InputStream
import java.util.zip.ZipInputStream

interface CoreUpdater {

    fun downloadCores(context: Context, coreIDs: List<CoreID>): Completable

    interface CoreManagerApi {
        @GET
        @Streaming
        fun downloadFile(@Url url: String): Single<Response<InputStream>>

        @GET
        @Streaming
        fun downloadZip(@Url url: String): Single<Response<ZipInputStream>>
    }
}
