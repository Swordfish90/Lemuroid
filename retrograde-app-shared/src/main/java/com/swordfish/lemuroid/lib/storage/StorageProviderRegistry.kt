/*
 * GameLibraryProviderRegistry.kt
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.lib.storage

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.swordfish.lemuroid.lib.library.db.entity.Game

class StorageProviderRegistry(context: Context, val providers: Set<StorageProvider>) {
    companion object {
        const val PREF_NAME = "storage_providers"
    }

    private val providersByScheme =
        mapOf(
            *providers.map { provider ->
                provider.uriSchemes.map { scheme -> scheme to provider }
            }.flatten().toTypedArray(),
        )

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val enabledProviders: Iterable<StorageProvider>
        get() = providers.filter { prefs.getBoolean(it.id, it.enabledByDefault) }

    fun getProvider(game: Game): StorageProvider {
        val uri = Uri.parse(game.fileUri)
        return providersByScheme[uri.scheme]!!
    }
}
