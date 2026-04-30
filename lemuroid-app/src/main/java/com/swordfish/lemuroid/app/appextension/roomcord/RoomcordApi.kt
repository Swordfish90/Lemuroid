/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension.roomcord

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.swordfish.lemuroid.app.appextension.remoteconfig.IRemoteConfigFetcher
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface RoomcordApi {
    @POST("rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: String,
        @Body request: RoomcordMessageRequest
    ): okhttp3.ResponseBody

    @Multipart
    @POST("rooms/{roomId}/images")
    suspend fun uploadImage(
        @Path("roomId") roomId: String,
        @Part image: MultipartBody.Part
    ): okhttp3.ResponseBody
}

class RoomcordApiImpl @Inject constructor(private val remoteConfig: IRemoteConfigFetcher) {
    private var roomcordRetrofit: Retrofit? = null

    init {
        roomcordRetrofit = Retrofit.Builder()
            .baseUrl("https://rooms-api.wizeup.app/api/v1/")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(createOkHttpClientBuilder().build())
            .build()
    }

    val api = roomcordRetrofit?.create(RoomcordApi::class.java)

    private fun createOkHttpClientBuilder() = OkHttpClient.Builder().apply {
        readTimeout(30, TimeUnit.SECONDS)
        connectTimeout(30, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        addInterceptor(RoomcordBotInterceptor(remoteConfig))
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }
}

@Keep
data class RoomcordMessageRequest(
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String = "image",
    @SerializedName("attachmentUrl") val attachmentUrl: String? = null,
    @SerializedName("attachmentUrls") val attachmentUrls: List<String>? = null
)

@Keep
data class UploadResponse(
    @SerializedName("url") val url: String
)
