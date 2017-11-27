/*
 * ArchiveOrgStorageProvider.kt
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

import android.content.Context
import android.net.Uri
import android.support.v17.preference.LeanbackPreferenceFragment
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.metadata.GameMetadataProvider
import com.codebutler.odyssey.lib.storage.StorageFile
import com.codebutler.odyssey.lib.storage.StorageProvider
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File

class ArchiveOrgStorageProvider(private val context: Context) : StorageProvider {

    private val api: ArchiveOrgApi = Retrofit.Builder()
            .baseUrl("https://archive.org")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()))
            .build()
            .create(ArchiveOrgApi::class.java)

    override val id: String = "archiveorg"

    override val name: String = "Archive.org Internet Arcade"

    override val uriSchemes: List<String> = listOf("archiveorg")

    override val prefsFragmentClass: Class<out LeanbackPreferenceFragment>? = null

    override val metadataProvider: GameMetadataProvider = ArchiveOrgMetadataProvider(api)

    override fun listFiles(): Single<Iterable<StorageFile>> {
        return api.advancedSearch()
                .map { body -> body.response.docs
                        .map { doc ->
                            val uri = Uri.Builder()
                                    .scheme(uriSchemes.first())
                                    .authority(doc.identifier)
                                    .build()
                            StorageFile(doc.identifier, 0, null, uri)
                        }
                }
    }

    override fun getGameRom(game: Game): Single<File> {
        val gamesCacheDir = File(context.cacheDir, "archiveorg-games")
        gamesCacheDir.mkdirs()
        val gameFile = File(gamesCacheDir, Uri.parse(game.fileName).lastPathSegment)
        if (gameFile.exists()) {
            return Single.just(gameFile)
        }
        Timber.d("Downloading game: ${game.fileName}")
        return api.downloadFile(game.fileName)
                .map {
                    gameFile.writeBytes(it.bytes())
                    gameFile
                }
    }

    override fun getGameSave(game: Game): Single<Optional<ByteArray>> = Single.just(None)

    override fun setGameSave(game: Game, data: ByteArray): Completable = Completable.complete()

    private class ArchiveOrgMetadataProvider(private val api: ArchiveOrgApi) : GameMetadataProvider {
        override fun transformer(startedAtMs: Long): ObservableTransformer<StorageFile, Optional<Game>> {
            return ObservableTransformer { upstream ->
                upstream.flatMapSingle { file ->
                    val identifier = file.uri.authority
                    api.details(identifier)
                            .map { response ->
                                val romUrl = getRomUrl(response) ?: return@map None
                                Game(
                                        fileName = romUrl.toString(),
                                        fileUri = file.uri,
                                        title = getTitle(response),
                                        systemId = "arcade",
                                        developer = response.metadata.creator?.first(),
                                        coverFrontUrl = response.misc.image,
                                        lastIndexedAt = startedAtMs
                                ).toOptional()
                            }
                }
            }
        }

        private fun getTitle(response: ArchiveOrgApi.DetailsResponse) =
                response.metadata.title.first().replace(Regex("^Internet Arcade: "), "")

        private fun getRomUrl(response: ArchiveOrgApi.DetailsResponse): Uri? {
            val zipFilePath = response.files.entries.find { (_, value) -> value.format == "ZIP" }?.key ?: return null
            return Uri.Builder()
                    .scheme("https")
                    .authority(response.server)
                    .path(response.dir)
                    .appendEncodedPath(zipFilePath)
                    .build()
        }
    }
}
