/*
 * RetrogradeApplicationComponent.kt
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

import com.swordfish.lemuroid.app.shared.library.CoreUpdateWork
import com.swordfish.lemuroid.app.shared.library.LibraryIndexWork
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.app.shared.storage.cache.CacheCleanerWork
import com.swordfish.lemuroid.app.tv.LemuroidTVApplicationModule
import com.swordfish.lemuroid.app.tv.channel.ChannelUpdateWork
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjectionModule
import com.swordfish.lemuroid.lib.injection.PerApp
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AndroidWorkerInjectionModule::class,
        LemuroidApplicationModule::class,
        LibraryIndexWork.Module::class,
        SaveSyncWork.Module::class,
        ChannelUpdateWork.Module::class,
        CoreUpdateWork.Module::class,
        CacheCleanerWork.Module::class,
        LemuroidTVApplicationModule::class,
    ],
)
@PerApp
interface LemuroidApplicationComponent : AndroidInjector<LemuroidApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<LemuroidApplication>()
}
