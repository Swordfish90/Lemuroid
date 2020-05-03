package com.swordfish.lemuroid.lib.bios

import com.swordfish.lemuroid.lib.library.SystemID

data class Bios(
    val fileName: String,
    val crc32: String,
    val md5: String,
    val description: String,
    val systemID: SystemID
)
