/*
 * OdysseyApplicationModule.kt
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

package com.codebutler.odyssey.app

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Context
import com.codebutler.odyssey.app.feature.game.GameActivity
import com.codebutler.odyssey.app.feature.main.MainActivity
import com.codebutler.odyssey.app.feature.settings.SettingsActivity
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import com.codebutler.odyssey.lib.library.provider.local.LocalGameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

@Module(subcomponents = arrayOf(
        MainActivity.Component::class,
        GameActivity.Component::class,
        SettingsActivity.Component::class))
abstract class OdysseyApplicationModule {

    @Binds
    abstract fun context(app: OdysseyApplication): Context

    @Binds
    @IntoMap
    @ActivityKey(MainActivity::class)
    abstract fun mainActivityInjectorFactory(builder: MainActivity.Component.Builder):
            AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(GameActivity::class)
    abstract fun gameActivityInjectorFactory(builder: GameActivity.Component.Builder):
            AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(SettingsActivity::class)
    abstract fun settingsActivityInjectorFactory(builder: SettingsActivity.Component.Builder):
            AndroidInjector.Factory<out Activity>

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun executorService(): ExecutorService = Executors.newSingleThreadExecutor()

        @Provides
        @JvmStatic
        fun ovgdbManager(app: OdysseyApplication, executorService: ExecutorService) = OvgdbManager(app, executorService)

        @Provides
        @JvmStatic
        fun odysseyDb(app: OdysseyApplication)
                = Room.databaseBuilder(app, OdysseyDatabase::class.java, OdysseyDatabase.DB_NAME)
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @IntoSet
        @JvmStatic
        fun localGameLibraryProvider(app: OdysseyApplication): GameLibraryProvider = LocalGameLibraryProvider(app)

        @Provides
        @JvmStatic
        fun gameLibraryProviderRegistry(providers: Set<@JvmSuppressWildcards GameLibraryProvider>)
                = GameLibraryProviderRegistry(providers)

        @Provides
        @JvmStatic
        fun gameLibrary(
                db: OdysseyDatabase,
                ovgdbManager: OvgdbManager,
                gameLibraryProviderRegistry: GameLibraryProviderRegistry)
                = GameLibrary(db, ovgdbManager, gameLibraryProviderRegistry)

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
        fun coreManager(app: OdysseyApplication, retrofit: Retrofit)
                = CoreManager(retrofit, File(app.cacheDir, "cores"))
    }
}
