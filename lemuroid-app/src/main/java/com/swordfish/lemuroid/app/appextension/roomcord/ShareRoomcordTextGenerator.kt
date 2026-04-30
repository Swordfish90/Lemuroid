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

import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareRoomcordTextGenerator @Inject constructor(
    private val roomcordManager: RoomcordManager,
    private val roomcordImageUploader: RoomcordImageUploader
) {

    fun shareGame(game: Game, content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        GlobalScope.launch {
            try {
                val bucketUrl = roomcordImageUploader.uploadGameImage(game)

                val request = if (bucketUrl != null) {
                    RoomcordMessageRequest(
                        content = content,
                        type = "image",
                        attachmentUrl = bucketUrl,
                        attachmentUrls = listOf(bucketUrl)
                    )
                } else {
                    RoomcordMessageRequest(
                        content = content,
                        type = "text"
                    )
                }

                roomcordManager.sendMessage(request)
                withContext(Dispatchers.Main) { onSuccess.invoke() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError.invoke(e.message ?: "Unknown error") }
            }
        }
    }
}
