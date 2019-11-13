/*
 * WebDavLibraryProvider.kt
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

package com.codebutler.retrograde.storage.webdav

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import androidx.leanback.preference.LeanbackPreferenceFragment
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.library.metadata.GameMetadataProvider
import com.codebutler.retrograde.lib.storage.StorageFile
import com.codebutler.retrograde.lib.storage.StorageProvider
import com.codebutler.retrograde.storage.webdav.client.WebDavClient
import com.codebutler.retrograde.storage.webdav.client.WebDavScanner
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import okhttp3.Credentials
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber

class WebDavStorageProvider(
    private val context: Context,
    override val metadataProvider: GameMetadataProvider
) : StorageProvider {

    private val webDavClient: WebDavClient
    private val webDavScanner: WebDavScanner

    init {
        val loggingInterceptor = HttpLoggingInterceptor { message -> Timber.d(message) }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .addInterceptor(loggingInterceptor)
                .addNetworkInterceptor { chain ->
                    val config = readConfig()
                    if (config.username != null && config.password != null) {
                        val credentials = Credentials.basic(config.username, config.password)
                        chain.proceed(chain.request().newBuilder()
                                .header("Authorization", credentials)
                                .build())
                    } else {
                        chain.proceed(chain.request())
                    }
                }
                .build()

        webDavClient = WebDavClient(okHttpClient, XmlPullParserFactory.newInstance())
        webDavScanner = WebDavScanner(webDavClient)
    }

    override val id: String = "webdav"

    override val name: String = context.getString(R.string.webdav_webdav)

    override val uriSchemes = listOf("webdav", "webdavs")

    override val prefsFragmentClass: Class<out LeanbackPreferenceFragment>? = WebDavPreferenceFragment::class.java

    override val enabledByDefault = false

    override fun listFiles(): Single<Iterable<StorageFile>> = Single.fromCallable {
        val url = readConfig().url
        if (url.isNullOrBlank()) {
            return@fromCallable listOf<StorageFile>()
        }
        val baseUri = URI.create(url)
        webDavScanner.scan(baseUri)
                .filter { davResponse -> davResponse.propStat?.prop?.displayName?.startsWith(".")?.not() ?: false }
                .map { davResponse ->
                    val displayName = URLDecoder.decode(davResponse.propStat?.prop?.displayName, "UTF-8")
                    val contentLength = davResponse.propStat?.prop?.contentLength ?: 0
                    val uri = UriTransformer(Uri.parse(baseUri.resolve(davResponse.href).toString())).libraryUri
                    StorageFile(displayName, contentLength, null, uri)
                }
                .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> {
        val gamesCacheDir = File(context.cacheDir, "dav-games")
        gamesCacheDir.mkdirs()
        val gameFile = File(gamesCacheDir, game.fileName)
        if (gameFile.exists()) {
            return Single.just(gameFile)
        }
        val httpUri = UriTransformer(game.fileUri).httpUri
        Timber.d("Downloading game: $httpUri")
        return webDavClient.downloadFile(httpUri)
                .map { bytes ->
                    gameFile.writeBytes(bytes)
                    gameFile
                }
    }

    override fun getGameSave(game: Game): Single<Optional<ByteArray>> =
            webDavClient.downloadFile(getSaveUri(game))
            .map { saveData -> saveData.toOptional() }
            .onErrorReturnItem(None)

    override fun setGameSave(game: Game, data: ByteArray): Completable =
            webDavClient.uploadFile(getSaveUri(game), data)

    private fun readConfig(): Configuration {
        val prefs = context.getSharedPreferences(WebDavPreferenceFragment.PREFS_NAME, MODE_PRIVATE)
        return Configuration(
                prefs.getString(context.getString(R.string.webdav_pref_key_url), null),
                prefs.getString(context.getString(R.string.webdav_pref_key_username), null),
                prefs.getString(context.getString(R.string.webdav_pref_key_password), null))
    }

    private fun getSaveUri(game: Game) = Uri.parse(readConfig().url)
            .buildUpon()
            .appendPath(".retrograde")
            .appendPath("saves")
            .appendPath(game.fileName + ".sram")
            .build()

    private data class Configuration(
        val url: String?,
        val username: String?,
        val password: String?
    )

    /**
     * Games URIs are webdav:// or webdavs:// in the game database,
     * but need to be http:// or https:// for the http client.
     */
    private class UriTransformer(val uri: Uri) {
        val httpUri: Uri
            get() = when (uri.scheme) {
                "http", "https" -> uri
                "webdav" -> uri.replaceScheme("http")
                "webdavs" -> uri.replaceScheme("https")
                else -> throw IllegalArgumentException()
            }

        val libraryUri: Uri
            get() = when (uri.scheme) {
                "webdav", "webdavs" -> uri
                "http" -> uri.replaceScheme("webdav")
                "https" -> uri.replaceScheme("webdavs")
                else -> throw IllegalArgumentException()
            }

        private fun Uri.replaceScheme(newScheme: String): Uri = this.buildUpon().scheme(newScheme).build()
    }
}
