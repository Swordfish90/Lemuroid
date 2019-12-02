/*
 * RetrogradeApplicationModule.kt
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

package com.codebutler.retrograde.app

import android.content.Context
import android.preference.PreferenceManager
import androidx.room.Room
import com.codebutler.retrograde.app.feature.game.GameActivity
import com.codebutler.retrograde.app.feature.game.GameLauncherActivity
import com.codebutler.retrograde.app.feature.main.MainActivity
import com.codebutler.retrograde.lib.core.CoreManager
import com.codebutler.retrograde.lib.game.GameLoader
import com.codebutler.retrograde.lib.injection.PerActivity
import com.codebutler.retrograde.lib.injection.PerApp
import com.codebutler.retrograde.lib.library.GameLibrary
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.dao.GameSearchDao
import com.codebutler.retrograde.lib.logging.RxTimberTree
import com.codebutler.retrograde.lib.storage.StorageProvider
import com.codebutler.retrograde.lib.storage.StorageProviderRegistry
import com.codebutler.retrograde.lib.storage.accessframework.StorageAccessFrameworkProvider
import com.codebutler.retrograde.lib.storage.local.LocalStorageProvider
import com.codebutler.retrograde.metadata.libretrodb.LibretroDBMetadataProvider
import com.codebutler.retrograde.metadata.libretrodb.db.LibretroDBManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.Type
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

@Module
abstract class RetrogradeApplicationModule {

    @Binds
    abstract fun context(app: RetrogradeApplication): Context

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivity.Module::class])
    abstract fun mainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun gameLauncherActivity(): GameLauncherActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun gameActivity(): GameActivity

    @Module
    companion object {
        @Provides
        @PerApp
        @JvmStatic
        fun executorService(): ExecutorService = Executors.newSingleThreadExecutor()

        @Provides
        @PerApp
        @JvmStatic
        fun ovgdbManager(app: RetrogradeApplication, executorService: ExecutorService) =
                LibretroDBManager(app, executorService)

        @Provides
        @PerApp
        @JvmStatic
        fun retrogradeDb(app: RetrogradeApplication) =
                Room.databaseBuilder(app, RetrogradeDatabase::class.java, RetrogradeDatabase.DB_NAME)
                        .addCallback(GameSearchDao.CALLBACK)
                        .addMigrations(GameSearchDao.MIGRATION)
                        .fallbackToDestructiveMigration()
                        .build()

        @Provides
        @PerApp
        @JvmStatic
        fun ovgdbMetadataProvider(ovgdbManager: LibretroDBManager) = LibretroDBMetadataProvider(ovgdbManager)

        @Provides
        @PerApp
        @IntoSet
        @JvmStatic
        fun localSAFStorageProvider(context: Context, metadataProvider: LibretroDBMetadataProvider): StorageProvider =
                StorageAccessFrameworkProvider(context, metadataProvider)

        @Provides
        @PerApp
        @IntoSet
        @JvmStatic
        fun localGameStorageProvider(context: Context, metadataProvider: LibretroDBMetadataProvider): StorageProvider =
                LocalStorageProvider(context, metadataProvider, true)

        @Provides
        @PerApp
        @JvmStatic
        fun gameStorageProviderRegistry(context: Context, providers: Set<@JvmSuppressWildcards StorageProvider>) =
                StorageProviderRegistry(context, providers)

        @Provides
        @PerApp
        @JvmStatic
        fun gameLibrary(
            db: RetrogradeDatabase,
            storageProviderRegistry: StorageProviderRegistry
        ) =
                GameLibrary(db, storageProviderRegistry)

        @Provides
        @PerApp
        @JvmStatic
        fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build()

        @Provides
        @PerApp
        @JvmStatic
        fun retrofit(): Retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .baseUrl("https://example.com")
                .addConverterFactory(object : Converter.Factory() {
                    override fun responseBodyConverter(
                        type: Type?,
                        annotations: Array<out Annotation>?,
                        retrofit: Retrofit?
                    ): Converter<ResponseBody, *>? {
                        if (type == ZipInputStream::class.java) {
                            return Converter<ResponseBody, ZipInputStream> { responseBody ->
                                ZipInputStream(responseBody.byteStream())
                            }
                        }
                        return null
                    }
                })
                .build()

        @Provides
        @PerApp
        @JvmStatic
        fun coreManager(context: Context, retrofit: Retrofit) = CoreManager(context, retrofit)

        @Provides
        @PerApp
        @JvmStatic
        fun rxTree() = RxTimberTree()

        @Provides
        @PerApp
        @JvmStatic
        fun rxPrefs(context: Context) =
                RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(context))

        @Provides
        @PerApp
        @JvmStatic
        fun gameLoader(coreManager: CoreManager, retrogradeDatabase: RetrogradeDatabase, gameLibrary: GameLibrary) =
                GameLoader(coreManager, retrogradeDatabase, gameLibrary)
    }
}
