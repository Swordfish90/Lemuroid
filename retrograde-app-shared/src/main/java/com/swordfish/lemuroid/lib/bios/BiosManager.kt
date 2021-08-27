package com.swordfish.lemuroid.lib.bios

import com.swordfish.lemuroid.common.files.safeDelete
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.StorageFile
import timber.log.Timber
import java.io.File
import java.io.InputStream

class BiosManager(private val directoriesManager: DirectoriesManager) {

    fun deleteBiosBefore(timestampMs: Long) {
        Timber.i("Pruning old bios files")
        SUPPORTED_BIOS
            .map { File(directoriesManager.getSystemDirectory(), it.fileName) }
            .filter { it.lastModified() < normalizeTimestamp(timestampMs) }
            .forEach {
                Timber.d("Pruning old bios file: ${it.path}")
                it.safeDelete()
            }
    }

    fun getBiosInfo(): BiosInfo {
        val bios = SUPPORTED_BIOS.groupBy {
            File(directoriesManager.getSystemDirectory(), it.fileName).exists()
        }.withDefault { listOf() }

        return BiosInfo(bios.getValue(true), bios.getValue(false))
    }

    fun tryAddBiosAfter(storageFile: StorageFile, inputStream: InputStream, timestampMs: Long): Boolean {
        val bios = SUPPORTED_BIOS.firstOrNull { it.crc32 == storageFile.crc } ?: return false

        Timber.i("Importing bios file: $bios")

        val biosFile = File(directoriesManager.getSystemDirectory(), bios.fileName)
        if (biosFile.exists() && biosFile.setLastModified(normalizeTimestamp(timestampMs))) {
            Timber.d("Bios file already present. Updated last modification date.")
        } else {
            Timber.d("Bios file not available. Copying new file.")
            inputStream.writeToFile(biosFile)
        }
        return true
    }

    private fun normalizeTimestamp(timestamp: Long) = (timestamp / 1000) * 1000

    data class BiosInfo(val detected: List<Bios>, val notDetected: List<Bios>)

    companion object {
        private val SUPPORTED_BIOS = listOf(
            Bios(
                "scph101.bin",
                "171BDCEC",
                "6E3735FF4C7DC899EE98981385F6F3D0",
                "PS One 4.5 NTSC-U/C",
                SystemID.PSX
            ),
            Bios(
                "scph7001.bin",
                "502224B6",
                "1E68C231D0896B7EADCAD1D7D8E76129",
                "PS Original 4.1 NTSC-U/C",
                SystemID.PSX
            ),
            Bios(
                "scph5501.bin",
                "8D8CB7E4",
                "490F666E1AFB15B7362B406ED1CEA246",
                "PS Original 3.0 NTSC-U/C",
                SystemID.PSX
            ),
            Bios(
                "scph1001.bin",
                "37157331",
                "924E392ED05558FFDB115408C263DCCF",
                "PS Original 2.2 NTSC-U/C",
                SystemID.PSX
            ),
            Bios(
                "lynxboot.img",
                "0D973C9D",
                "FCD403DB69F54290B51035D82F835E7B",
                "Lynx Boot Image",
                SystemID.LYNX
            ),
            Bios(
                "bios_CD_E.bin",
                "529AC15A",
                "E66FA1DC5820D254611FDCDBA0662372",
                "Sega CD E",
                SystemID.SEGACD
            ),
            Bios(
                "bios_CD_J.bin",
                "9D2DA8F2",
                "278A9397D192149E84E820AC621A8EDD",
                "Sega CD J",
                SystemID.SEGACD
            ),
            Bios(
                "bios_CD_U.bin",
                "C6D10268",
                "2EFD74E3232FF260E371B99F84024F7F",
                "Sega CD U",
                SystemID.SEGACD
            ),
            Bios(
                "bios7.bin",
                "1280F0D5",
                "DF692A80A5B1BC90728BC3DFC76CD948",
                "Nintendo DS ARM7",
                SystemID.NDS
            ),
            Bios(
                "bios9.bin",
                "2AB23573",
                "A392174EB3E572FED6447E956BDE4B25",
                "Nintendo DS ARM9",
                SystemID.NDS
            ),
            Bios(
                "firmware.bin",
                "945F9DC9",
                "E45033D9B0FA6B0DE071292BBA7C9D13",
                "Nintendo DS Firmware",
                SystemID.NDS
            )
        )
    }
}
