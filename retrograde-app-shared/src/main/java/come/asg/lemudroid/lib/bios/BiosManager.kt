package come.asg.lemudroid.lib.bios

import android.os.FileObserver
import come.asg.lemudroid.lib.library.SystemCoreConfig
import come.asg.lemudroid.lib.library.SystemID
import come.asg.lemudroid.lib.library.db.entity.Game
import come.asg.lemudroid.lib.storage.DirectoriesManager
import come.asg.lemudroid.lib.storage.StorageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

class BiosManager(private val directoriesManager: DirectoriesManager) {
    private val biosByMd5 = SUPPORTED_BIOS.associateBy { it.md5.lowercase(Locale.US) }
    private val biosByName = SUPPORTED_BIOS.associateBy { it.libretroFileName }

    /** BIOS is optional for the GB/GBC/GBA systems supported by this app. */
    @Suppress("UNUSED_PARAMETER")
    fun getMissingBiosFiles(
        coreConfig: SystemCoreConfig,
        game: Game,
    ): List<String> = emptyList()

    fun deleteBiosBefore(timestampMs: Long) {
        // BIOS images are user-managed system files, not scan-owned library entries.
        // Keep this method as a compatibility no-op for existing callers.
    }

    fun observeBiosChanges(onChanged: () -> Unit): FileObserver =
        object : FileObserver(
            directoriesManager.getSystemDirectory().absolutePath,
            FileObserver.CREATE or
                FileObserver.CLOSE_WRITE or
                FileObserver.MOVED_TO or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE or
                FileObserver.ATTRIB,
        ) {
            override fun onEvent(
                event: Int,
                path: String?,
            ) {
                if (path == null || SUPPORTED_BIOS.any { it.libretroFileName == path }) {
                    onChanged()
                }
            }
        }

    @Deprecated("Use the suspend variant")
    fun getBiosInfo(): BiosInfo {
        val systemDirectory = directoriesManager.getSystemDirectory()
        val detected = mutableListOf<Bios>()
        val notDetected = mutableListOf<Bios>()
        val checksumMismatches = mutableListOf<Bios>()

        SUPPORTED_BIOS.forEach { bios ->
            val file = File(systemDirectory, bios.libretroFileName)
            val fileMd5 = if (file.isFile) runCatching { md5(file.inputStream()) }.getOrNull() else null
            when {
                fileMd5 == null -> notDetected += bios
                bios.matchesChecksum(fileMd5) -> detected += bios
                else -> {
                    notDetected += bios
                    checksumMismatches += bios
                }
            }
        }
        return BiosInfo(detected, notDetected, checksumMismatches)
    }

    suspend fun getBiosInfoAsync(): BiosInfo =
        withContext(Dispatchers.IO) {
            getBiosInfo()
        }

    /**
     * Imports only content whose MD5 matches a supported BIOS. Unknown filenames are considered
     * only when their reported size could be one of the small BIOS images, avoiding hashing ROMs.
     */
    fun tryAddBiosAfter(
        storageFile: StorageFile,
        inputStream: InputStream,
        timestampMs: Long,
    ): Boolean {
        val expectedName = biosByName[storageFile.name]
        val isPossibleBiosSize = SUPPORTED_BIOS.any { storageFile.size in it.possibleFileSizesBytes }
        if (expectedName == null && !isPossibleBiosSize) {
            inputStream.close()
            return false
        }

        val (fileMd5, content) = inputStream.use { hashAndCapture(it) }
        val bios = biosByMd5[fileMd5] ?: return false
        val bytes = content ?: return false
        Timber.i("Importing verified bios file ${storageFile.name} as ${bios.libretroFileName}")

        val biosFile = File(directoriesManager.getSystemDirectory(), bios.libretroFileName)
        val currentMd5 = if (biosFile.isFile) runCatching { md5(biosFile.inputStream()) }.getOrNull() else null
        if (!bios.matchesChecksum(currentMd5.orEmpty())) {
            biosFile.outputStream().use { it.write(bytes) }
        }
        biosFile.setLastModified(normalizeTimestamp(timestampMs))
        return true
    }

    private fun hashAndCapture(inputStream: InputStream): Pair<String, ByteArray?> {
        val digest = MessageDigest.getInstance("MD5")
        val captured = ByteArrayOutputStream(MAX_BIOS_SIZE_BYTES)
        val buffer = ByteArray(BUFFER_SIZE)
        var totalBytes = 0
        var canCapture = true
        while (true) {
            val read = inputStream.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
            totalBytes += read
            if (canCapture && totalBytes <= MAX_BIOS_SIZE_BYTES) {
                captured.write(buffer, 0, read)
            } else {
                canCapture = false
            }
        }
        val checksum = digest.digest().joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        return checksum to if (canCapture) captured.toByteArray() else null
    }

    private fun md5(inputStream: InputStream): String = inputStream.use { hashAndCapture(it).first }

    private fun normalizeTimestamp(timestamp: Long) = (timestamp / 1000) * 1000

    data class BiosInfo(
        val detected: List<Bios>,
        val notDetected: List<Bios>,
        val checksumMismatches: List<Bios> = emptyList(),
    )

    companion object {
        private const val BUFFER_SIZE = 8 * 1024
        private const val MAX_BIOS_SIZE_BYTES = 16 * 1024

        private val SUPPORTED_BIOS =
            listOf(
                Bios(
                    "gba_bios.bin",
                    "a860e8c0b6d573d191e4ec7db1b1e4f6",
                    "Game Boy Advance BIOS",
                    SystemID.GBA,
                    setOf(16 * 1024L),
                ),
                Bios(
                    "gb_bios.bin",
                    "32fbbd84168d3482956eb3c5051637f5",
                    "Game Boy BIOS",
                    SystemID.GB,
                    setOf(256L),
                ),
                Bios(
                    "gbc_bios.bin",
                    "dbfce9db9deaa2567f6a84fde55f9680",
                    "Game Boy Color BIOS",
                    SystemID.GBC,
                    setOf(2_304L),
                ),
                Bios(
                    "sgb_bios.bin",
                    "d574d4f9c12f305074798f54c091a8b4",
                    "Super Game Boy BIOS",
                    SystemID.GB,
                    setOf(256L, 2_048L),
                ),
            )
    }
}
