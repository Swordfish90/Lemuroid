/*
 * OdysseyApplicationComponent.kt
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

import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import com.codebutler.odyssey.provider.gdrive.GDriveApplicationModule
import dagger.BindsInstance
import dagger.Component

@Component(modules = arrayOf(OdysseyApplicationModule::class, GDriveApplicationModule::class))
interface OdysseyApplicationComponent {

    fun coreManager(): CoreManager

    fun gameLibrary(): GameLibrary

    fun gameLibraryProviderRegistry(): GameLibraryProviderRegistry

    fun odysseyDb(): OdysseyDatabase

    fun inject(app: OdysseyApplication)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: OdysseyApplication): Builder

        fun build(): OdysseyApplicationComponent
    }
}
