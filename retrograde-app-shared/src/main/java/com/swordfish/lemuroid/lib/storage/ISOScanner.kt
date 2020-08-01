package com.swordfish.lemuroid.lib.storage

import com.swordfish.lemuroid.common.files.FileUtils
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.roundToInt
import com.swordfish.lemuroid.common.kotlin.indexOf
import com.swordfish.lemuroid.common.kotlin.kb
import com.swordfish.lemuroid.common.kotlin.mb

object ISOScanner {
    private val WINDOW_SIZE = 8.kb()
    private val READ_BUFFER_SIZE = 64.kb()

    private val PS_HEADER_MAX_SIZE = 4.mb()
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
        return extractPlayStationSerial(fileName, inputStream.buffered(READ_BUFFER_SIZE))
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
        val skipSize = WINDOW_SIZE - PS_SERIAL_MAX_SIZE

        movingWidnowSequence(stream, WINDOW_SIZE, (skipSize).toLong())
            .take(ceil(PS_HEADER_MAX_SIZE.toDouble() / skipSize.toDouble()).roundToInt())
            .flatMap { serial ->
                (baseSerials).asSequence()
                    .map { serial.indexOf(it) }
                    .filter { it >= 0 }
                    .map { serial to it }
            }
            .map { (bytes, index) ->
                val serialBytes = bytes.copyOfRange(index, index + PS_SERIAL_MAX_SIZE)
                val serial = String(serialBytes, Charsets.US_ASCII)
                PS_SERIAL_REGEX.find(serial)?.groupValues?.let { "${it[1]}-${it[2]}" }
            }
            .filterNotNull()
            .firstOrNull()
    }

    private fun movingWidnowSequence(inputStream: InputStream, windowSize: Int, windowSkip: Long) = sequence {
        val buffer = ByteArray(windowSize)
        do {
            inputStream.mark(windowSize)
            yield(readByteArray(inputStream, buffer))
            inputStream.reset()
        } while (inputStream.skip(windowSkip) != 0L)
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
