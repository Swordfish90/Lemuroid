package com.swordfish.lemuroid.lib.storage

import com.swordfish.lemuroid.common.files.FileUtils
import java.io.InputStream

class ISOScanner {
    companion object {
        private const val PS_HEADER_MAX_SIZE = 1024 * 1024
        private const val PS_SERIAL_MAX_SIZE = 10

        private val psSerialRegex = Regex("^([A-Z]+)-?([0-9]+)")

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

        /** Extract a PS1 or PSP serial from ISO file or PBP. */
        private fun extractPlayStationSerial(fileName: String, inputStream: InputStream) = inputStream.use { inputStream ->
            if (FileUtils.extractExtension(fileName) !in setOf("iso" , "pbp")) {
                return null
            }

            movingWidnowSequence(inputStream, PS_SERIAL_MAX_SIZE)
                .take(PS_HEADER_MAX_SIZE)
                .map { String(it, Charsets.US_ASCII) }
                .filter { serial -> (PSP_BASE_SERIALS + PSX_BASE_SERIALS).any { serial.startsWith(it) } }
                .map { serial ->
                    psSerialRegex.find(serial)?.groupValues?.let { "${it[1]}-${it[2]}" }
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
}
