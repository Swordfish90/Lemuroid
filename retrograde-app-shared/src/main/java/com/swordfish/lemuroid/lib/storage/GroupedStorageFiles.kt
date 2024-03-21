package com.swordfish.lemuroid.lib.storage

data class GroupedStorageFiles(
    val primaryFile: BaseStorageFile,
    val dataFiles: List<BaseStorageFile>,
) {
    fun allFiles() = listOf(primaryFile) + dataFiles
}
