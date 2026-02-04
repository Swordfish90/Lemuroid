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
import android.content.SharedPreferences
import androidx.room.Room
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadBindingActivity
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadShortcutBindingActivity
import com.swordfish.lemuroid.app.mobile.feature.main.MainActivity
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.shared.game.ExternalGameLauncherActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.rumble.RumbleManager
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.shared.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.app.tv.channel.ChannelHandler
import com.swordfish.lemuroid.app.tv.settings.BiosPreferences
import com.swordfish.lemuroid.app.tv.settings.CoresSelectionPreferences
import com.swordfish.lemuroid.ext.feature.core.CoreUpdaterImpl
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.ext.feature.savesync.SaveSyncManagerImpl
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerApp
import com.swordfish.lemuroid.lib.library.LemuroidLibrary
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.GameSearchDao
import com.swordfish.lemuroid.lib.library.db.dao.Migrations
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.migration.DesmumeMigrationHandler
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.saves.SavesCoherencyEngine
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.StorageProvider
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import com.swordfish.lemuroid.lib.storage.local.LocalStorageProvider
import com.swordfish.lemuroid.lib.storage.local.StorageAccessFrameworkProvider
import com.swordfish.lemuroid.metadata.libretrodb.LibretroDBMetadataProvider
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.InputStream
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

@Module
abstract class LemuroidApplicationModule {
    @Binds
    abstract fun context(app: LemuroidApplication): Context

    @Binds
    abstract fun saveSyncManager(saveSyncManagerImpl: SaveSyncManagerImpl): SaveSyncManager

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivity.Module::class])
    abstract fun mainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun externalGameLauncherActivity(): ExternalGameLauncherActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun gameActivity(): GameActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [GameMenuActivity.Module::class])
    abstract fun gameMenuActivity(): GameMenuActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun storageFrameworkPickerLauncher(): StorageFrameworkPickerLauncher

    @PerActivity
    @ContributesAndroidInjector(modules = [GamePadBindingActivity.Module::class])
    abstract fun gamepadBindingActivity(): GamePadBindingActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [GamePadShortcutBindingActivity.Module::class])
    abstract fun gamepadShortcutBindingActivity(): GamePadShortcutBindingActivity

    @Module
    companion object {
        @Provides
        @PerApp
        @JvmStatic
        fun libretroDBManager(app: LemuroidApplication) = LibretroDBManager(app)

        @Provides
        @PerApp
        @JvmStatic
        fun retrogradeDb(app: LemuroidApplication) =
            Room.databaseBuilder(app, RetrogradeDatabase::class.java, RetrogradeDatabase.DB_NAME)
                .addCallback(GameSearchDao.CALLBACK)
                .addMigrations(GameSearchDao.MIGRATION, Migrations.VERSION_8_9)
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @PerApp
        @JvmStatic
        fun gameMetadataProvider(libretroDBManager: LibretroDBManager): GameMetadataProvider =
            LibretroDBMetadataProvider(libretroDBManager)

        @Provides
        @PerApp
        @IntoSet
        @JvmStatic
        fun localSAFStorageProvider(context: Context): StorageProvider = StorageAccessFrameworkProvider(context)

        @Provides
        @PerApp
        @IntoSet
        @JvmStatic
        fun localGameStorageProvider(
            context: Context,
            directoriesManager: DirectoriesManager,
        ): StorageProvider = LocalStorageProvider(context, directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun gameStorageProviderRegistry(
            context: Context,
            providers: Set<@JvmSuppressWildcards StorageProvider>,
        ) = StorageProviderRegistry(context, providers)

        @Provides
        @PerApp
        @JvmStatic
        fun lemuroidLibrary(
            db: RetrogradeDatabase,
            storageProviderRegistry: Lazy<StorageProviderRegistry>,
            gameMetadataProvider: Lazy<GameMetadataProvider>,
            biosManager: BiosManager,
        ) = LemuroidLibrary(db, storageProviderRegistry, gameMetadataProvider, biosManager)

        @Provides
        @PerApp
        @JvmStatic
        fun okHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build()

        @Provides
        @PerApp
        @JvmStatic
        fun retrofit(): Retrofit =
            Retrofit.Builder()
                .baseUrl("https://example.com")
                .addConverterFactory(
                    object : Converter.Factory() {
                        override fun responseBodyConverter(
                            type: Type,
                            annotations: Array<out Annotation>,
                            retrofit: Retrofit,
                        ): Converter<ResponseBody, *>? {
                            if (type == ZipInputStream::class.java) {
                                return Converter<ResponseBody, ZipInputStream> { responseBody ->
                                    ZipInputStream(responseBody.byteStream())
                                }
                            }
                            if (type == InputStream::class.java) {
                                return Converter<ResponseBody, InputStream> { responseBody ->
                                    responseBody.byteStream()
                                }
                            }
                            return null
                        }
                    },
                )
                .build()

        @Provides
        @PerApp
        @JvmStatic
        fun directoriesManager(context: Context) = DirectoriesManager(context)

        @Provides
        @PerApp
        @JvmStatic
        fun statesManager(directoriesManager: DirectoriesManager) = StatesManager(directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun savesManager(directoriesManager: DirectoriesManager) = SavesManager(directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun statesPreviewManager(directoriesManager: DirectoriesManager) = StatesPreviewManager(directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun coreManager(
            directoriesManager: DirectoriesManager,
            retrofit: Retrofit,
        ): CoreUpdater = CoreUpdaterImpl(directoriesManager, retrofit)

        @Provides
        @PerApp
        @JvmStatic
        fun coreVariablesManager(sharedPreferences: Lazy<SharedPreferences>) = CoreVariablesManager(sharedPreferences)

        @Provides
        @PerApp
        @JvmStatic
        fun gameLoader(
            lemuroidLibrary: LemuroidLibrary,
            statesManager: StatesManager,
            savesManager: SavesManager,
            coreVariablesManager: CoreVariablesManager,
            retrogradeDatabase: RetrogradeDatabase,
            savesCoherencyEngine: SavesCoherencyEngine,
            directoriesManager: DirectoriesManager,
            biosManager: BiosManager,
            desmumeMigrationHandler: DesmumeMigrationHandler,
        ) = GameLoader(
            lemuroidLibrary,
            statesManager,
            savesManager,
            coreVariablesManager,
            retrogradeDatabase,
            savesCoherencyEngine,
            directoriesManager,
            biosManager,
            desmumeMigrationHandler,
        )

        @Provides
        @PerApp
        @JvmStatic
        fun inputDeviceManager(
            context: Context,
            sharedPreferences: Lazy<SharedPreferences>,
        ) = InputDeviceManager(context, sharedPreferences)

        @Provides
        @PerApp
        @JvmStatic
        fun biosManager(directoriesManager: DirectoriesManager) = BiosManager(directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun biosPreferences(biosManager: BiosManager) = BiosPreferences(biosManager)

        @Provides
        @PerApp
        @JvmStatic
        fun coresSelection(
            sharedPreferences: Lazy<SharedPreferences>,
            desmumeMigrationHandler: DesmumeMigrationHandler,
        ) = CoresSelection(sharedPreferences, desmumeMigrationHandler)

        @Provides
        @PerApp
        @JvmStatic
        fun coreSelectionPreferences() = CoresSelectionPreferences()

        @Provides
        @PerApp
        @JvmStatic
        fun savesCoherencyEngine(
            savesManager: SavesManager,
            statesManager: StatesManager,
        ) = SavesCoherencyEngine(savesManager, statesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun saveSyncManagerImpl(
            context: Context,
            directoriesManager: DirectoriesManager,
        ) = SaveSyncManagerImpl(context, directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun desmumeMigrationHandler(directoriesManager: DirectoriesManager) =
            DesmumeMigrationHandler(directoriesManager)

        @Provides
        @PerApp
        @JvmStatic
        fun postGameHandler(retrogradeDatabase: RetrogradeDatabase) =
            GameLaunchTaskHandler(ReviewManager(), retrogradeDatabase)

        @Provides
        @PerApp
        @JvmStatic
        fun shortcutsGenerator(
            context: Context,
            retrofit: Retrofit,
        ) = ShortcutsGenerator(context, retrofit)

        @Provides
        @PerApp
        @JvmStatic
        fun channelHandler(
            context: Context,
            retrogradeDatabase: RetrogradeDatabase,
            retrofit: Retrofit,
        ) = ChannelHandler(context, retrogradeDatabase, retrofit)

        @Provides
        @PerApp
        @JvmStatic
        fun retroControllerManager(sharedPreferences: Lazy<SharedPreferences>) =
            ControllerConfigsManager(sharedPreferences)

        @Provides
        @PerApp
        @JvmStatic
        fun settingsManager(
            context: Context,
            sharedPreferences: Lazy<SharedPreferences>,
        ) = SettingsManager(context, sharedPreferences)

        @Provides
        @PerApp
        @JvmStatic
        fun sharedPreferences(context: Context) = SharedPreferencesHelper.getSharedPreferences(context)

        @Provides
        @PerApp
        @JvmStatic
        fun gameLauncher(
            coresSelection: CoresSelection,
            gameLaunchTaskHandler: GameLaunchTaskHandler,
        ) = GameLauncher(coresSelection, gameLaunchTaskHandler)

        @Provides
        @PerApp
        @JvmStatic
        fun rumbleManager(
            context: Context,
            settingsManager: SettingsManager,
            inputDeviceManager: InputDeviceManager,
        ) = RumbleManager(context, settingsManager, inputDeviceManager)
    }
}
