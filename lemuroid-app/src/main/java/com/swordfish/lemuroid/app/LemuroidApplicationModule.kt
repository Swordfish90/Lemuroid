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

package com.swordfish.lemuroid.app

import android.content.Context
import android.preference.PreferenceManager
import androidx.room.Room
import com.swordfish.lemuroid.app.feature.game.GameActivity
import com.swordfish.lemuroid.app.feature.game.GameLauncherActivity
import com.swordfish.lemuroid.app.feature.main.MainActivity
import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerApp
import com.swordfish.lemuroid.lib.library.GameLibrary
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.GameSearchDao
import com.swordfish.lemuroid.lib.logging.RxTimberTree
import com.swordfish.lemuroid.lib.storage.StorageProvider
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import com.swordfish.lemuroid.lib.storage.accessframework.StorageAccessFrameworkProvider
import com.swordfish.lemuroid.lib.storage.local.LocalStorageProvider
import com.swordfish.lemuroid.metadata.libretrodb.LibretroDBMetadataProvider
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.swordfish.lemuroid.app.feature.coreoptions.CoreOptionsActivity
import com.swordfish.lemuroid.app.feature.settings.SettingsManager
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
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
abstract class LemuroidApplicationModule {

    @Binds
    abstract fun context(app: LemuroidApplication): Context

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivity.Module::class])
    abstract fun mainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun gameLauncherActivity(): GameLauncherActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun retroOptionsActivity(): CoreOptionsActivity

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
        fun ovgdbManager(app: LemuroidApplication, executorService: ExecutorService) =
                LibretroDBManager(app, executorService)

        @Provides
        @PerApp
        @JvmStatic
        fun retrogradeDb(app: LemuroidApplication) =
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
        fun localSAFStorageProvider(
            context: Context,
            metadataProvider: LibretroDBMetadataProvider
        ): StorageProvider = StorageAccessFrameworkProvider(context, metadataProvider)

        @Provides
        @PerApp
        @IntoSet
        @JvmStatic
        fun localGameStorageProvider(
            context: Context,
            directoriesManager: DirectoriesManager,
            metadataProvider: LibretroDBMetadataProvider
        ): StorageProvider =
                LocalStorageProvider(context, directoriesManager, metadataProvider)

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
        ) = GameLibrary(db, storageProviderRegistry)

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
        fun directoriesManager(context: Context) = DirectoriesManager(context)

        @Provides
        @PerApp
        @JvmStatic
        fun savesManager(directoriesManager: DirectoriesManager) = SavesManager(directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun coreManager(
            directoriesManager: DirectoriesManager,
            retrofit: Retrofit
        ) = CoreManager(directoriesManager, retrofit)

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
        fun coreVariablesManager(context: Context) = CoreVariablesManager(context)

        @Provides
        @PerApp
        @JvmStatic
        fun gameLoader(
            coreManager: CoreManager,
            retrogradeDatabase: RetrogradeDatabase,
            gameLibrary: GameLibrary,
            savesManager: SavesManager,
            coreVariablesManager: CoreVariablesManager
        ) = GameLoader(coreManager, retrogradeDatabase, gameLibrary, savesManager, coreVariablesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun settingsManager(context: Context) = SettingsManager(context)
    }
}
