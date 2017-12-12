/*
 * GDriveIntegrationModule.kt
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.storage.gdrive

import android.app.Activity
import com.codebutler.odyssey.lib.injection.PerApp
import com.codebutler.odyssey.lib.storage.StorageProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

@Module(subcomponents = [GDriveComponent::class])
abstract class GDriveApplicationModule {

    @Binds
    @IntoSet
    @PerApp
    abstract fun bindsProviderIntoSet(provider: GDriveStorageProvider): StorageProvider

    @Module
    companion object {

        @Provides
        @PerApp
        @JvmStatic
        fun gameLibraryProvider(componentBuilder: GDriveComponent.Builder) =
                GDriveStorageProvider(componentBuilder)

        @Provides
        @IntoMap
        @ActivityKey(GDriveBrowseActivity::class)
        @JvmStatic
        fun activityInjectorFactory(provider: GDriveStorageProvider): AndroidInjector.Factory<out Activity> =
                provider.component.activityComponentBuilder()
    }
}
