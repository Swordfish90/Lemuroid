package com.swordfish.lemuroid.common.files

class FileUtils {
    companion object {
        fun extractExtension(fileName: String): String? = fileName.substringAfterLast(".")
    }
}
