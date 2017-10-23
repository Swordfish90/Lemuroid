/*
 * MainComponent.kt
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

package com.codebutler.odyssey.app.feature.main

import com.codebutler.odyssey.app.OdysseyApplicationComponent
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import dagger.BindsInstance
import dagger.Component

@Component(
        dependencies = arrayOf(OdysseyApplicationComponent::class),
        modules = arrayOf(MainModule::class))
interface MainComponent {

    fun coreManager(): CoreManager

    fun gameLibrary(): GameLibrary

    fun gameLibraryProviderRegistry(): GameLibraryProviderRegistry

    fun odysseyDb(): OdysseyDatabase

    fun inject(activity: MainActivity)

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: OdysseyApplicationComponent): Builder

        fun module(module: MainModule): Builder

        @BindsInstance
        fun activity(activity: MainActivity): Builder

        fun build(): MainComponent
    }
}
