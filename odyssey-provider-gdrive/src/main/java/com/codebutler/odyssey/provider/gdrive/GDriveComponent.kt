/*
 * Component.kt
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

import dagger.Subcomponent
import javax.inject.Scope

@GDriveScope
@Subcomponent(modules = arrayOf(GDriveModule::class))
interface GDriveComponent {

    fun activitySubcomponentBuilder(): GDriveBrowseActivityComponent.Builder

    fun gdriveBrowser(): GDriveBrowser

    fun inject(provider: GDriveGameLibraryProvider)

    @Subcomponent.Builder
    interface Builder {
        fun build(): GDriveComponent
    }
}

@Scope
annotation class GDriveScope
