/*
 * ArchiveOrgApi.kt
 *
 * Copyright (C) 2017 Odyssey Project
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

package com.codebutler.odyssey.storage.archiveorg

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

internal interface ArchiveOrgApi {

    @GET("/advancedsearch.php?q=collection:internetarcade&fl=identifier&rows=999&output=json")
    fun advancedSearch(): Single<AdvancedSearchResponse>

    @GET("/details/{identifier}?output=json")
    fun details(@Path("identifier") identifier: String): Single<DetailsResponse>

    @GET
    fun downloadFile(@Url url: String): Single<ResponseBody>

    data class AdvancedSearchResponse(val response: Response) {
        data class Response(val docs: List<Doc>) {
            data class Doc(val identifier: String)
        }
    }

    data class DetailsResponse(
            val server: String,
            val dir: String,
            val metadata: Metadata,
            val files: Map<String, File>,
            val misc: Misc) {
        data class Metadata(val title: List<String>, val creator: List<String>?)
        data class File(val format: String)
        data class Misc(val image: String)
    }
}
