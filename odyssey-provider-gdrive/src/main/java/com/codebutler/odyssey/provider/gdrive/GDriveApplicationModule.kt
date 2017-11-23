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

package com.codebutler.odyssey.provider.gdrive

import android.app.Activity
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

@Module(subcomponents = arrayOf(GDriveComponent::class))
abstract class GDriveApplicationModule {

    @Binds
    @IntoSet
    abstract fun bindsProviderIntoSet(provider: GDriveGameLibraryProvider): GameLibraryProvider

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun gameLibraryProvider(componentBuilder: GDriveComponent.Builder)
                = GDriveGameLibraryProvider(componentBuilder)

        @Provides
        @IntoMap
        @JvmStatic
        @ActivityKey(GDriveBrowseActivity::class)
        fun activityInjectorFactory(provider: GDriveGameLibraryProvider): AndroidInjector.Factory<out Activity>
                = provider.component.activityComponentBuilder()
    }
}
