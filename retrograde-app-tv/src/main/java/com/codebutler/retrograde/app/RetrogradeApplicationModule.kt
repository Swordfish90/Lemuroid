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

import android.arch.persistence.room.Room
import android.content.Context
import com.codebutler.retrograde.app.feature.game.GameActivity
import com.codebutler.retrograde.app.feature.main.MainActivity
import com.codebutler.retrograde.app.feature.settings.SettingsActivity
import com.codebutler.retrograde.lib.core.CoreManager
import com.codebutler.retrograde.lib.injection.PerActivity
import com.codebutler.retrograde.lib.library.GameLibrary
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.ovgdb.db.OvgdbMetadataProvider
import com.codebutler.retrograde.lib.storage.StorageProvider
import com.codebutler.retrograde.lib.storage.StorageProviderRegistry
import com.codebutler.retrograde.lib.storage.local.LocalStorageProvider
import com.codebutler.retrograde.metadata.ovgdb.db.OvgdbManager
import com.codebutler.retrograde.storage.archiveorg.ArchiveOrgStorageProvider
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
    @ContributesAndroidInjector(modules = [GameActivity.Module::class])
    abstract fun gameActivity(): GameActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [SettingsActivity.Module::class])
    abstract fun settingsActivity(): SettingsActivity

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun executorService(): ExecutorService = Executors.newSingleThreadExecutor()

        @Provides
        @JvmStatic
        fun ovgdbManager(app: RetrogradeApplication, executorService: ExecutorService) = OvgdbManager(app, executorService)

        @Provides
        @JvmStatic
        fun retrogradeDb(app: RetrogradeApplication) =
                Room.databaseBuilder(app, RetrogradeDatabase::class.java, RetrogradeDatabase.DB_NAME)
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @JvmStatic
        fun ovgdbMetadataProvider(ovgdbManager: OvgdbManager) = OvgdbMetadataProvider(ovgdbManager)

        @Provides
        @IntoSet
        @JvmStatic
        fun localGameStorageProvider(context: Context, metadataProvider: OvgdbMetadataProvider): StorageProvider =
                LocalStorageProvider(context, metadataProvider)

        @Provides
        @IntoSet
        @JvmStatic
        fun archiveorgStorageProvider(context: Context): StorageProvider = ArchiveOrgStorageProvider(context)

        @Provides
        @JvmStatic
        fun gameStorageProviderRegistry(context: Context, providers: Set<@JvmSuppressWildcards StorageProvider>) =
                StorageProviderRegistry(context, providers)

        @Provides
        @JvmStatic
        fun gameLibrary(
                db: RetrogradeDatabase,
                storageProviderRegistry: StorageProviderRegistry) =
                GameLibrary(db, storageProviderRegistry)

        @Provides
        @JvmStatic
        fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build()

        @Provides
        @JvmStatic
        fun retrofit(): Retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .baseUrl("https://example.com")
                .addConverterFactory(object : Converter.Factory() {
                    override fun responseBodyConverter(
                            type: Type?,
                            annotations: Array<out Annotation>?,
                            retrofit: Retrofit?): Converter<ResponseBody, *>? {
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
        @JvmStatic
        fun coreManager(context: Context, retrofit: Retrofit) = CoreManager(context, retrofit)
    }
}
