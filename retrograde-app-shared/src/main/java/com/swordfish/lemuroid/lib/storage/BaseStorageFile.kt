package com.swordfish.lemuroid.lib.storage

import android.net.Uri

data class BaseStorageFile(
    val name: String,
    val size: Long,
    val uri: Uri,
    val path: String? = null,
) {
    val extension: String
        get() = name.substringAfterLast('.', "")

    val extensionlessName: String
        get() = name.substringBeforeLast('.', "")
}
