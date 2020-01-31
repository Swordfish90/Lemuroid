package com.swordfish.lemuroid.lib.storage

import com.swordfish.lemuroid.common.files.FileUtils
import java.io.InputStream

class ISOScanner {
    companion object {
        private const val PSP_HEADER_MAX_SIZE = 1024 * 1024
        private const val PSP_SERIAL_SIZE = 10

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
            return extractPSPSerial(fileName, inputStream)
        }

        private fun extractPSPSerial(fileName: String, inputStream: InputStream): String? = inputStream.buffered().use { bufferedInputStream ->
            if (FileUtils.extractExtension(fileName) != "iso") {
                return null
            }

            movingWidnowSequence(bufferedInputStream, PSP_SERIAL_SIZE)
                .take(PSP_HEADER_MAX_SIZE)
                .map { String(it, Charsets.US_ASCII) }
                .filter { serial -> PSP_BASE_SERIALS.any { serial.startsWith(it) } }
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
