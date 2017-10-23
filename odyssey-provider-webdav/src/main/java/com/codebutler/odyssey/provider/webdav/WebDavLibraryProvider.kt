/*
 * WebDavLibraryProvider.kt
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

package com.codebutler.odyssey.provider.webdav

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.support.v17.preference.LeanbackPreferenceFragment
import android.util.Log
import com.codebutler.odyssey.lib.library.GameLibraryFile
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import com.codebutler.odyssey.lib.webdav.WebDavClient
import com.codebutler.odyssey.lib.webdav.WebDavScanner
import io.reactivex.Single
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class WebDavLibraryProvider(private val context: Context) : GameLibraryProvider {

    companion object {
        const val TAG = "WebDavLibraryProvider"
    }

    private val httpClient: OkHttpClient
    private val webDavClient: WebDavClient
    private val webDavScanner: WebDavScanner

    init {
        httpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
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
        webDavClient = WebDavClient(httpClient, XmlPullParserFactory.newInstance())
        webDavScanner = WebDavScanner(webDavClient)
    }

    override val name: String = context.getString(R.string.webdav_webdav)

    override val uriSchemes = listOf("webdav", "webdavs")

    override val prefsFragmentClass: Class<out LeanbackPreferenceFragment>? = WebDavPreferenceFragment::class.java

    override fun listFiles(): Single<Iterable<GameLibraryFile>> = Single.fromCallable {
        val url = readConfig().url ?: return@fromCallable listOf<GameLibraryFile>()
        val baseUri = URI.create(url)
        webDavScanner.scan(baseUri)
                .filter { davResponse -> davResponse.propStat?.prop?.displayName?.startsWith(".")?.not() ?: false }
                .map { davResponse ->
                    val displayName = URLDecoder.decode(davResponse.propStat?.prop?.displayName, "UTF-8")
                    val contentLength = davResponse.propStat?.prop?.contentLength ?: 0
                    val uri = UriTransformer(Uri.parse(baseUri.resolve(davResponse.href).toString())).libraryUri
                    GameLibraryFile(displayName, contentLength, null, uri)
                }
                .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val gamesCacheDir = File(context.cacheDir, "dav-games")
        gamesCacheDir.mkdirs()
        val gameFile = File(gamesCacheDir, game.fileName)
        if (!gameFile.exists()) {
            val httpUri = UriTransformer(game.fileUri).httpUri
            Log.d(TAG, "Downloading game: $httpUri")
            gameFile.writeBytes(webDavClient.downloadFile(httpUri))
        }
        gameFile
    }

    override fun getGameSave(coreId: String, game: Game): Single<ByteArray> {
        TODO()
    }

    override fun setGameSave(coreId: String, game: Game, data: ByteArray): Single<Unit> {
        TODO()
    }

    private fun readConfig(): Configuration {
        val prefs = context.getSharedPreferences(WebDavPreferenceFragment.PREFS_NAME, MODE_PRIVATE)
        return Configuration(
                prefs.getString(context.getString(R.string.webdav_pref_key_url), null),
                prefs.getString(context.getString(R.string.webdav_pref_key_username), null),
                prefs.getString(context.getString(R.string.webdav_pref_key_password), null))
    }

    private data class Configuration(
            val url: String?,
            val username: String?,
            val password: String?)

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
