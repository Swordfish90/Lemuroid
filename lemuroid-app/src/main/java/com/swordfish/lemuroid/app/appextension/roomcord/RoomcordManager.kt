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

package com.swordfish.lemuroid.app.appextension.roomcord

import android.util.Log
import com.swordfish.lemuroid.app.appextension.remoteconfig.IRemoteConfigFetcher
import com.swordfish.lemuroid.app.appextension.remoteconfig.getRoomcordBotToken
import com.swordfish.lemuroid.app.appextension.remoteconfig.getRoomcordRoomId

class RoomcordManager(
    private val roomcordApiImpl: RoomcordApiImpl,
    private val remoteConfig: IRemoteConfigFetcher
) {

    suspend fun sendMessage(data: RoomcordMessageRequest) {
        val roomId = remoteConfig.getRoomcordRoomId()
        val token = remoteConfig.getRoomcordBotToken()
        Log.d(TAG, "sendMessage: roomId='$roomId' tokenEmpty=${token.isEmpty()} content='${data.content}'")

        check(roomId.isNotEmpty()) { "Roomcord room ID is not configured (game_maker_story_room_id)" }
        check(token.isNotEmpty()) { "Roomcord token is not configured (game_maker_story)" }

        val response = roomcordApiImpl.api?.sendMessage(roomId, data)
        val responseBody = response?.string()
        Log.d(TAG, "sendMessage: response body=$responseBody")
    }

    companion object {
        private const val TAG = "RoomcordManager"
    }
}
