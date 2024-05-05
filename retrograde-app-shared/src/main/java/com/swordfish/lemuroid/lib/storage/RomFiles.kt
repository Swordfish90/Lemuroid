package com.swordfish.lemuroid.lib.storage

import android.os.ParcelFileDescriptor
import java.io.File

sealed class RomFiles {
    data class Standard(val files: List<File>) : RomFiles()

    data class Virtual(val files: List<Entry>) : RomFiles() {
        data class Entry(val filePath: String, val fd: ParcelFileDescriptor)
    }
}
