/*
 * GDriveBrowser.kt
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

package com.codebutler.odyssey.provider.gdrive

import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import io.reactivex.Single
import java.io.FileOutputStream
import kotlin.coroutines.experimental.buildSequence

class GDriveBrowser(private val driveFactory: DriveFactory) {

    fun downloadById(fileId: String, stream: FileOutputStream) {
        val drive = driveFactory.create().toNullable() ?: throw IllegalStateException()
        drive.files()
                .get(fileId)
                .executeMediaAndDownloadTo(stream)
    }

    fun downloadByName(space: String = "drive", parentFolderId: String?, fileName: String): ByteArray? {
        val drive = driveFactory.create().toNullable() ?: throw IllegalStateException()
        val existingFile = getFileMetadata(drive, space, parentFolderId, fileName)
        val fileId = existingFile?.id ?: return null
        return drive.files()
                .get(fileId)
                .executeMediaAsInputStream()
                .use { stream -> stream.readBytes() }
    }

    fun uploadByName(space: String = "drive", parentFolderId: String?, fileName: String, data: ByteArray) {
        val drive = driveFactory.create().toNullable() ?: throw IllegalStateException()
        val content = ByteArrayContent("application/octet-stream", data)
        val existingFile = getFileMetadata(drive, space, parentFolderId, fileName)
        if (existingFile != null) {
            drive.files().update(existingFile.id, null, content)
                    .execute()
        } else {
            val newFile = File()
            newFile.name = fileName
            if (parentFolderId != null) {
                newFile.parents = listOf(parentFolderId)
            }
            drive.files()
                    .create(newFile, content)
                    .setFields("id, parents, name")
                    .execute()
        }
    }

    fun list(parentId: String): Single<List<File>> = Single.fromCallable {
        val drive = driveFactory.create().toNullable() ?: return@fromCallable listOf<File>()
        val mutableList = mutableListOf<File>()
        var pageToken: String? = null
        do {
            val result = drive.files().list()
                .setQ("'$parentId' in parents")
                .setFields("nextPageToken, files(id, name, mimeType, size)").setPageToken(pageToken).execute()
            mutableList.addAll(result.files)
            pageToken = result.nextPageToken
        } while (pageToken != null)
        mutableList.toList()
    }

    fun listRecursive(folderId: String): Sequence<File> {
        val drive = driveFactory.create().toNullable() ?: return emptySequence()
        return listRecursive(drive, folderId)
    }

    private fun getFileMetadata(drive: Drive, space: String, parentId: String?, fileName: String): File? {
        val q = if (parentId != null) {
            "'$parentId' in parents and name = '$fileName'"
        } else {
            "name = '$fileName'"
        }
        return drive.files().list()
                .setSpaces(space)
                .setQ(q)
                .execute()
                .files
                .firstOrNull()
    }

    private fun listRecursive(drive: Drive, folderId: String): Sequence<File> {
        var pageToken: String? = null
        return buildSequence {
            do {
                val result = drive.files().list()
                        .setQ("'$folderId' in parents")
                        .setFields("nextPageToken, files(id, name, mimeType, size)")
                        .setPageToken(pageToken)
                        .execute()
                for (file in result.files) {
                    if (file.mimeType == "application/vnd.google-apps.folder") {
                        yieldAll(listRecursive(drive, file.id))
                    } else {
                        yield(file)
                    }
                }
                pageToken = result.nextPageToken
            } while (pageToken != null)
        }
    }
}
