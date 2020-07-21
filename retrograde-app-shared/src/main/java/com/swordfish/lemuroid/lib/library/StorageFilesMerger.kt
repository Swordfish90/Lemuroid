package com.swordfish.lemuroid.lib.library

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.common.files.readLines
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.GroupedStorageFiles

object StorageFilesMerger {

    /** Merge files which belong to the same game. This includes bin/cue files and m3u playlists.*/
    fun mergeDataFiles(context: Context, files: List<BaseStorageFile>): List<GroupedStorageFiles> {
        val allFiles = files
                .map { it to listOf<BaseStorageFile>() }
                .toMap().toMutableMap()

        val toBeRemoved = mutableListOf<BaseStorageFile>()

        allFiles.keys
                .filter { it.extension == "cue" }
                .forEach { cueFile ->
                    val binFiles = allFiles
                            .filter { it.key.extension == "bin" && it.key.extensionlessName == cueFile.extensionlessName }

                    allFiles[cueFile] = (allFiles[cueFile] ?: listOf()) + binFiles.flatMap { listOf(it.key) + it.value }
                    toBeRemoved.addAll(binFiles.keys)
                }
        toBeRemoved.forEach { allFiles.remove(it) }
        toBeRemoved.clear()

        allFiles.keys
                .filter { it.extension == "m3u" }
                .forEach { m3uFile ->
                    val documentFile = DocumentFile.fromSingleUri(context, m3uFile.uri)
                    val m3uFiles = documentFile?.readLines(context) ?: listOf()
                    val dataFiles = allFiles.filter { it.key.name in m3uFiles }

                    allFiles[m3uFile] = allFiles[m3uFile]!! + dataFiles.flatMap { listOf(it.key) + it.value }
                    toBeRemoved.addAll(dataFiles.keys)
                }
        toBeRemoved.forEach { allFiles.remove(it) }
        toBeRemoved.clear()

        return allFiles.map { GroupedStorageFiles(it.key, it.value) }
    }
}
