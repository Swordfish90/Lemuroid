/*
 * WebDavScanner.kt
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

package com.codebutler.retrograde.storage.webdav.client

import java.net.URI

class WebDavScanner(private val webDavClient: WebDavClient) {

    fun scan(uri: URI): Sequence<WebDavClient.DavResponse> {
        val responses = webDavClient.propfind(uri.toString())
        return sequence {
            for (davResponse in responses) {
                val collectionUri = uri.resolve(davResponse.href)
                if (collectionUri != uri) {
                    if (davResponse.propStat?.prop?.resourceType == WebDavClient.DavResourceType.COLLECTION) {
                        yieldAll(scan(collectionUri))
                    } else {
                        yield(davResponse)
                    }
                }
            }
        }
    }
}
