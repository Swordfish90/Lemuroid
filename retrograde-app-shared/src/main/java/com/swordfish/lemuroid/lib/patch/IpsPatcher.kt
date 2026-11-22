package com.swordfish.lemuroid.lib.patch

import java.io.ByteArrayOutputStream
import java.io.InputStream

sealed class IpsError(message: String) : Exception(message) {
    class InvalidMagic(msg: String = "File does not start with IPS 'PATCH' header") : IpsError(msg)
    class UnexpectedEof(msg: String = "Unexpected end of file before IPS 'EOF' marker") : IpsError(msg)
    class CorruptedRecord(msg: String) : IpsError(msg)
    class ExceedsMaxRomSize(msg: String = "Patch offset exceeds maximum allowed ROM size (64MB)") : IpsError(msg)
}

object IpsPatcher {
    private val IPS_HEADER = byteArrayOf(0x50, 0x41, 0x54, 0x43, 0x48)
    private const val EOF_OFFSET = 0x454F46
    private const val MAX_ROM_SIZE = 64 * 1024 * 1024

    fun isValidIps(patchBytes: ByteArray): Boolean {
        if (patchBytes.size < 8) return false
        for (i in IPS_HEADER.indices) {
            if (patchBytes[i] != IPS_HEADER[i]) return false
        }
        return true
    }

    /**
     * Applies a single ips patch from an inputstream onto a base ROM byte array
     * Returns a new bytearray containing the patched ROM
     */
    fun applyPatch(baseRom: ByteArray, patchStream: InputStream): ByteArray {
        val patchBytes = patchStream.readBytes()
        return applyPatch(baseRom, patchBytes)
    }

    fun applyPatch(baseRom: ByteArray, patchBytes: ByteArray): ByteArray {
        if (patchBytes.size < 8) {
            throw IpsError.InvalidMagic("Patch file too short (${patchBytes.size} bytes)")
        }

        for (i in IPS_HEADER.indices) {
            if (patchBytes[i] != IPS_HEADER[i]) {
                throw IpsError.InvalidMagic()
            }
        }

        var romBuffer = baseRom.copyOf()
        var cursor = 5

        while (cursor + 3 <= patchBytes.size) {
            val offset = ((patchBytes[cursor].toInt() and 0xFF) shl 16) or
                    ((patchBytes[cursor + 1].toInt() and 0xFF) shl 8) or
                    (patchBytes[cursor + 2].toInt() and 0xFF)
            cursor += 3
            if (offset == EOF_OFFSET) {
                if (cursor + 3 <= patchBytes.size) {
                    val truncateSize = ((patchBytes[cursor].toInt() and 0xFF) shl 16) or
                            ((patchBytes[cursor + 1].toInt() and 0xFF) shl 8) or
                            (patchBytes[cursor + 2].toInt() and 0xFF)
                    if (truncateSize in 0..MAX_ROM_SIZE) {
                        romBuffer = if (truncateSize <= romBuffer.size) {
                            romBuffer.copyOf(truncateSize)
                        } else {
                            romBuffer.copyOf(truncateSize)
                        }
                    }
                }
                return romBuffer
            }

            if (cursor + 2 > patchBytes.size) {
                throw IpsError.UnexpectedEof("Premature end of file reading record length at offset $offset")
            }

            val length = ((patchBytes[cursor].toInt() and 0xFF) shl 8) or
                    (patchBytes[cursor + 1].toInt() and 0xFF)
            cursor += 2

            if (length > 0) {
                if (cursor + length > patchBytes.size) {
                    throw IpsError.UnexpectedEof("Truncated record data at offset $offset (expected $length bytes)")
                }
                val endOffset = offset + length
                if (endOffset > MAX_ROM_SIZE) {
                    throw IpsError.ExceedsMaxRomSize()
                }

                if (endOffset > romBuffer.size) {
                    romBuffer = romBuffer.copyOf(endOffset)
                }

                System.arraycopy(patchBytes, cursor, romBuffer, offset, length)
                cursor += length
            } else {
                if (cursor + 3 > patchBytes.size) {
                    throw IpsError.UnexpectedEof("Truncated RLE record at offset $offset")
                }
                val rleLength = ((patchBytes[cursor].toInt() and 0xFF) shl 8) or
                        (patchBytes[cursor + 1].toInt() and 0xFF)
                val value = patchBytes[cursor + 2]
                cursor += 3

                if (rleLength <= 0) {
                    throw IpsError.CorruptedRecord("Invalid RLE length $rleLength at offset $offset")
                }

                val endOffset = offset + rleLength
                if (endOffset > MAX_ROM_SIZE) {
                    throw IpsError.ExceedsMaxRomSize()
                }

                if (endOffset > romBuffer.size) {
                    romBuffer = romBuffer.copyOf(endOffset)
                }

                romBuffer.fill(value, offset, endOffset)
            }
        }

        throw IpsError.UnexpectedEof()
    }

    fun applyPatches(baseRom: ByteArray, patches: List<ByteArray>): ByteArray {
        var current = baseRom
        for (patch in patches) {
            current = applyPatch(current, patch)
        }
        return current
    }
}
