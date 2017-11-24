/*
 * GDriveModule.kt
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

import android.content.Context
import dagger.Module
import dagger.Provides

@Module(subcomponents = arrayOf(GDriveBrowseActivity.Component::class))
internal abstract class GDriveModule {

    @Module
    companion object {

        @Provides
        @GDriveScope
        @JvmStatic
        fun driveFactory(context: Context) = DriveFactory(context)

        @Provides
        @GDriveScope
        @JvmStatic
        fun gdriveBrowser(driveFactory: DriveFactory) = GDriveBrowser(driveFactory)
    }
}
