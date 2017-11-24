/*
 * GameLibraryProviderRegistry.kt
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

package com.codebutler.odyssey.lib.storage

import com.codebutler.odyssey.lib.library.db.entity.Game

class StorageProviderRegistry(val providers: Set<StorageProvider>) {

    private val providersByScheme = mapOf(*providers.map { provider ->
        provider.uriSchemes.map { scheme ->
            scheme to provider
        }
    }.flatten().toTypedArray())

    fun getProvider(game: Game) = providersByScheme[game.fileUri.scheme]!!
}
