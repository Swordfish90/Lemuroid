package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.common.files.readLines
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.GroupedStorageFiles
import com.swordfish.lemuroid.lib.storage.StorageProvider

object StorageFilesMerger {

    /** Merge files which belong to the same game. This includes bin/cue files and m3u playlists.*/
    fun mergeDataFiles(
        storageProvider: StorageProvider,
        files: List<BaseStorageFile>
    ): List<GroupedStorageFiles> {
        val allFiles = files
            .map { it to listOf<BaseStorageFile>() }
            .toMap().toMutableMap()

        mergeBinCueFiles(allFiles)
        removeInvalidBinCuePairs(allFiles)
        mergeM3UPlaylists(allFiles, storageProvider)
        removeInvalidM3UPlaylists(allFiles, storageProvider)

        return allFiles.map { GroupedStorageFiles(it.key, it.value) }
    }

    private fun removeInvalidM3UPlaylists(
        allFiles: MutableMap<BaseStorageFile, List<BaseStorageFile>>,
        storageProvider: StorageProvider
    ) {
        val toBeRemoved = mutableListOf<BaseStorageFile>()

        allFiles.keys
            .asSequence()
            .filter { it.extension == "m3u" }
            .forEach { m3uFile ->
                val m3uFiles: List<String> = runCatching {
                    storageProvider.getInputStream(m3uFile.uri)?.readLines()
                }.getOrNull() ?: listOf()

                val filesNames = allFiles[m3uFile]?.map { it.name } ?: listOf()

                if (!filesNames.containsAll(m3uFiles)) {
                    toBeRemoved.add(m3uFile)
                }
            }

        toBeRemoved.forEach { allFiles.remove(it) }
    }

    private fun mergeM3UPlaylists(
        allFiles: MutableMap<BaseStorageFile, List<BaseStorageFile>>,
        storageProvider: StorageProvider
    ) {
        val toBeRemoved = mutableListOf<BaseStorageFile>()

        allFiles.keys
            .asSequence()
            .filter { it.extension == "m3u" }
            .forEach { m3uFile ->
                val m3uFiles = runCatching {
                    storageProvider.getInputStream(m3uFile.uri)?.readLines()
                }.getOrNull() ?: listOf()

                val dataFiles = allFiles.filter { it.key.name in m3uFiles }

                allFiles[m3uFile] = allFiles[m3uFile]!! + dataFiles.flatMap {
                    listOf(it.key) + it.value
                }
                toBeRemoved.addAll(dataFiles.keys)
            }

        toBeRemoved.forEach { allFiles.remove(it) }
    }

    private fun removeInvalidBinCuePairs(
        allFiles: MutableMap<BaseStorageFile, List<BaseStorageFile>>
    ) {
        val toBeRemoved = mutableListOf<BaseStorageFile>()

        allFiles.keys
            .asSequence()
            .filter { it.extension == "cue" }
            .forEach { if (allFiles[it].isNullOrEmpty()) toBeRemoved.add(it) }

        toBeRemoved.forEach { allFiles.remove(it) }
    }

    private fun mergeBinCueFiles(
        allFiles: MutableMap<BaseStorageFile, List<BaseStorageFile>>
    ) {
        val toBeRemoved = mutableListOf<BaseStorageFile>()

        allFiles.keys
            .asSequence()
            .filter { it.extension == "cue" }
            .forEach { cueFile ->
                val binFiles = allFiles
                    .filter {
                        val isBinFile = it.key.extension == "bin"
                        val filenamesMatch = it.key.extensionlessName == cueFile.extensionlessName
                        isBinFile && filenamesMatch
                    }

                allFiles[cueFile] = (allFiles[cueFile] ?: listOf()) + binFiles.flatMap {
                    listOf(it.key) + it.value
                }
                toBeRemoved.addAll(binFiles.keys)
            }

        toBeRemoved.forEach { allFiles.remove(it) }
    }
}
