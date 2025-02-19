package com.swordfish.lemuroid.common.files

import java.io.File

fun File.safeDelete() = exists() && delete()

class FileUtils {
    companion object {
        fun extractExtension(fileName: String): String = fileName.substringAfterLast(".", "").lowercase()

        fun discardExtension(fileName: String): String = fileName.substringBeforeLast(".")
    }
}
