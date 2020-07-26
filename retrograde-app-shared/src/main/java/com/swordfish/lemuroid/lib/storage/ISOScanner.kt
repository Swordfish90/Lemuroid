package com.swordfish.lemuroid.lib.storage

import com.swordfish.lemuroid.common.files.FileUtils
import java.io.InputStream

object ISOScanner {
    private const val PS_HEADER_MAX_SIZE = 4 * 1024 * 1024 // TODO FILIPPO... Check if this can be reduced since 4MB for every fine it's a lot
    private const val PS_SERIAL_MAX_SIZE = 10

    private val PS_SERIAL_REGEX = Regex("^([A-Z]+)-?([0-9]+)")

    private val PS_SUPPORTED_FORMATS = setOf("iso", "pbp", "bin")

    private val PSX_BASE_SERIALS = listOf(
            "CPCS",
            "SCES",
            "SIPS",
            "SLKA",
            "SLPS",
            "SLUS",
            "ESPM",
            "SLED",
            "SCPS",
            "SCAJ",
            "PAPX",
            "SLES",
            "HPS",
            "LSP",
            "SLPM",
            "SCUS",
            "SCED"
    )

    private val PSP_BASE_SERIALS = listOf(
            "ULES",
            "ULUS",
            "ULJS",
            "ULEM",
            "ULUM",
            "ULJM",
            "UCES",
            "UCUS",
            "UCJS",
            "UCAS",
            "NPEH",
            "NPUH",
            "NPJH",
            "NPEG",
            "NPUG",
            "NPJG",
            "NPHG",
            "NPEZ",
            "NPUZ",
            "NPJZ"
    )

    fun extractSerial(fileName: String, inputStream: InputStream): String? {
        return extractPlayStationSerial(fileName, inputStream.buffered())
    }

    private fun ByteArray.startsWith(byteArray: ByteArray): Boolean {
        for (i in byteArray.indices) {
            if (this[i] != byteArray[i]) return false
        }
        return true
    }

    /** Extract a PS1 or PSP serial from ISO file or PBP. */
    private fun extractPlayStationSerial(fileName: String, inputStream: InputStream) = inputStream.use { stream ->
        if (FileUtils.extractExtension(fileName) !in PS_SUPPORTED_FORMATS) {
            return null
        }

        if (inputStream.available() < PS_HEADER_MAX_SIZE) {
            return null
        }

        val baseSerials = (PSP_BASE_SERIALS + PSX_BASE_SERIALS).map { it.toByteArray(Charsets.US_ASCII) }

        movingWidnowSequence(stream, PS_SERIAL_MAX_SIZE)
            .take(PS_HEADER_MAX_SIZE)
            .filter { serial -> (baseSerials).any { serial.startsWith(it) } }
            .map { bytes ->
                PS_SERIAL_REGEX.find(String(bytes, Charsets.US_ASCII))?.groupValues?.let { "${it[1]}-${it[2]}" }
            }
            .filterNotNull()
            .firstOrNull()
    }

    private fun movingWidnowSequence(inputStream: InputStream, windowSize: Int) = sequence {
        val buffer = ByteArray(windowSize)
        do {
            inputStream.mark(windowSize)
            yield(readByteArray(inputStream, buffer))
            inputStream.reset()
        } while (inputStream.skip(1) != 0L)
    }

    private fun readByteArray(inputStream: InputStream, byteArray: ByteArray): ByteArray {
        val readBytes = inputStream.read(byteArray)
        return if (readBytes < byteArray.size) {
            byteArray.copyOf(readBytes)
        } else {
            byteArray
        }
    }
}
