/*
 * WebDavClient.kt
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

package com.codebutler.odyssey.lib.webdav

import android.net.Uri
import com.codebutler.odyssey.common.xml.XmlPullParserUtil.readText
import com.codebutler.odyssey.common.xml.XmlPullParserUtil.skip
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.coroutines.experimental.buildIterator

class WebDavClient(private val client: OkHttpClient, private val xmlPullParserFactory: XmlPullParserFactory) {

    companion object {
        private const val NS = "DAV:"
    }

    fun propfind(url: String): Iterator<DavResponse> {
        val response = client.newCall(Request.Builder()
                .url(url)
                .method("PROPFIND", null).build())
                .execute()

        val parser = xmlPullParserFactory.newPullParser()
        parser.setInput(response.body()!!.byteStream(), "UTF-8")
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.nextTag()

        return readMultiStatus(parser)
    }

    fun downloadFile(uri: Uri): ByteArray {
        val response = client.newCall(Request.Builder()
                .url(uri.toString())
                .method("GET", null)
                .build()
        ).execute()
        return response.body()!!.bytes()
    }

    private fun readMultiStatus(parser: XmlPullParser): Iterator<DavResponse> {
        return buildIterator {
            parser.require(XmlPullParser.START_TAG, NS, "multistatus")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "response" -> yield(readResponse(parser))
                    else -> skip(parser)
                }
            }
        }
    }

    private fun readResponse(parser: XmlPullParser): DavResponse {
        parser.require(XmlPullParser.START_TAG, NS, "response")

        var href: String? = null
        var propstat: DavPropStat? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            when (name) {
                "href" -> href = parser.nextText()
                "propstat" -> propstat = readPropstat(parser)
                else -> skip(parser)
            }
        }

        return DavResponse(href, propstat)
    }

    private fun readPropstat(parser: XmlPullParser): DavPropStat {
        parser.require(XmlPullParser.START_TAG, NS, "propstat")

        var prop: DavProp? = null
        var status: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "prop" -> prop = readProp(parser)
                "status" -> status = readText(parser)
                else -> skip(parser)
            }
        }

        return DavPropStat(prop, status)
    }

    private fun readProp(parser: XmlPullParser): DavProp {
        parser.require(XmlPullParser.START_TAG, NS, "prop")

        var creationDate: String? = null
        var displayName: String? = null
        var contentLength: Long = 0
        var resourceType: DavResourceType = DavResourceType.NONE

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "creationdate" -> creationDate = readText(parser)
                "displayname" -> displayName = readText(parser)
                "getcontentlength" -> contentLength = readText(parser).toLongOrNull() ?: 0
                "resourcetype" -> resourceType = readResourceType(parser)
                else -> skip(parser)
            }
        }

        return DavProp(creationDate, displayName, contentLength, resourceType)
    }

    private fun readResourceType(parser: XmlPullParser): DavResourceType {
        parser.require(XmlPullParser.START_TAG, NS, "resourcetype")

        var resourceType = DavResourceType.NONE

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "collection" -> resourceType = DavResourceType.COLLECTION
            }
            skip(parser)
        }
        return resourceType
    }

    enum class DavResourceType {
        COLLECTION,
        NONE
    }

    data class DavResponse(
            val href: String?,
            val propStat: DavPropStat?)

    data class DavPropStat(
            val prop: DavProp?,
            val status: String?)

    data class DavProp(
            val creationDate: String?,
            val displayName: String?,
            val contentLength: Long,
            val resourceType: DavResourceType)
}
