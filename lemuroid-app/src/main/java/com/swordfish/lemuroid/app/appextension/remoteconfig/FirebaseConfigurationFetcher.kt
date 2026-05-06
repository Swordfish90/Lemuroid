/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.or

class FirebaseConfigurationFetcher : IRemoteConfigFetcher {

    override fun fetch(force: Boolean) {
        try {
            val instance = FirebaseRemoteConfig.getInstance()

            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (force) 0L else 3600L)
                .build()

            instance.setConfigSettingsAsync(settings).addOnCompleteListener {
                instance.setDefaultsAsync(R.xml.config_defaults).addOnCompleteListener {
                    instance.fetchAndActivate()
                        .addOnSuccessListener { updated ->
                            Log.d(TAG, "Remote config fetched successfully, updated=$updated")
                            Log.d(TAG, "game_maker_story empty=${instance.getString("game_maker_story").isEmpty()}")
                            Log.d(TAG, "game_maker_story_room_id empty=${instance.getString("game_maker_story_room_id").isEmpty()}")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Remote config fetch failed", e)
                        }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Remote config init failed", ex)
        }
    }

    override fun getRemoteBoolean(value: String) =
        FirebaseRemoteConfig.getInstance().getBoolean(value)

    override fun getRemoteString(value: String) =
        FirebaseRemoteConfig.getInstance().getString(value)

    override fun getRemoteLong(value: String) =
        FirebaseRemoteConfig.getInstance().getLong(value)

    override fun getRemoteDouble(value: String) =
        FirebaseRemoteConfig.getInstance().getDouble(value)

    companion object {
        private const val TAG = "FirebaseRemoteConfig"
    }
}
