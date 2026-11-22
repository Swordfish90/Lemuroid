package com.swordfish.lemuroid.lib.cheats.cht

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.lib.cheats.CheatSupport
import com.swordfish.lemuroid.lib.cheats.CheatValidator
import com.swordfish.lemuroid.lib.cheats.ValidationResult
import com.swordfish.lemuroid.lib.library.db.dao.CheatDao
import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipInputStream

data class DiscoveredCht(
    val id: String,
    val name: String,
    val source: ChtSource,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DiscoveredCht
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class ChtSource {
    IN_ZIP,
    EXTERNAL_FILE,
}

object ChtDiscoveryManager {
    private const val PREFS_AUTO_CHT_HASH_PREFIX = "pref_key_game_auto_cht_hash_"
    private val MASTER_CODE_REGEX =
        Regex("""\b(Master\s*Code|\(M\)|\[M\]|Enable\s*Code)\b""", RegexOption.IGNORE_CASE)

    fun discoverChtFiles(context: Context, game: Game): List<DiscoveredCht> {
        val results = mutableListOf<DiscoveredCht>()
        val fileUri = Uri.parse(game.fileUri)
        val isZipped = runCatching {
            DocumentFile.fromSingleUri(context, fileUri)?.name?.endsWith(".zip", ignoreCase = true) == true ||
                game.fileName.endsWith(".zip", ignoreCase = true)
        }.getOrDefault(false)

        if (isZipped) {
            runCatching {
                context.contentResolver.openInputStream(fileUri)?.use { input ->
                    ZipInputStream(input).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory && entry.name.endsWith(".cht", ignoreCase = true)) {
                                val bytes = zip.readBytes()
                                val simpleName = File(entry.name).name
                                results.add(
                                    DiscoveredCht(
                                        id = "zip:${entry.name}",
                                        name = simpleName,
                                        source = ChtSource.IN_ZIP,
                                        content = bytes,
                                    ),
                                )
                            }
                            entry = zip.nextEntry
                        }
                    }
                }
            }.onFailure {
                Timber.e(it, "Failed to scan in-zip .cht files for game ${game.id}")
            }
        }

        runCatching {
            val romDoc = DocumentFile.fromSingleUri(context, fileUri)
            val romBaseName = (romDoc?.name ?: game.fileName).substringBeforeLast('.')

            val docId = runCatching { DocumentsContract.getDocumentId(fileUri) }.getOrNull()
            if (docId != null && (docId.contains('/') || docId.contains(':'))) {
                val parentDocId = if (docId.contains('/')) {
                    docId.substringBeforeLast('/')
                } else {
                    docId.substringBeforeLast(':') + ":"
                }

                val externalFolders = getExternalFolders(context)
                for (folder in externalFolders) {
                    val rootUri = Uri.parse(folder)
                    runCatching {
                        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, parentDocId)
                        val projection = arrayOf(
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_SIZE,
                        )
                        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                            while (cursor.moveToNext()) {
                                val childDocId = cursor.getString(0)
                                val childName = cursor.getString(1)
                                if (childName != null && childName.endsWith(".cht", ignoreCase = true)) {
                                    val childBase = childName.substringBeforeLast('.')
                                    val matches = childBase.equals(romBaseName, ignoreCase = true) ||
                                        childBase.startsWith(romBaseName, ignoreCase = true) ||
                                        childBase.contains(romBaseName, ignoreCase = true) ||
                                        romBaseName.contains(childBase, ignoreCase = true)
                                    if (matches) {
                                        val childUri = DocumentsContract.buildDocumentUriUsingTree(rootUri, childDocId)
                                        val bytes = context.contentResolver.openInputStream(childUri)?.use { it.readBytes() }
                                        if (bytes != null && bytes.isNotEmpty()) {
                                            results.add(
                                                DiscoveredCht(
                                                    id = "file:$childDocId",
                                                    name = childName,
                                                    source = ChtSource.EXTERNAL_FILE,
                                                    content = bytes,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to scan external .cht files for game ${game.id}")
        }

        if (fileUri.scheme == "file" || (fileUri.path?.startsWith("/") == true && fileUri.scheme != "content")) {
            runCatching {
                val file = File(fileUri.path ?: "")
                val romBaseName = file.nameWithoutExtension
                val parent = file.parentFile
                if (parent != null && parent.isDirectory) {
                    parent.listFiles()?.filter { it.isFile && it.name.endsWith(".cht", ignoreCase = true) }?.forEach { child ->
                        val childBase = child.nameWithoutExtension
                        val matches = childBase.equals(romBaseName, ignoreCase = true) ||
                            childBase.startsWith(romBaseName, ignoreCase = true) ||
                            childBase.contains(romBaseName, ignoreCase = true) ||
                            romBaseName.contains(childBase, ignoreCase = true)
                        if (matches) {
                            val bytes = child.readBytes()
                            if (bytes.isNotEmpty()) {
                                results.add(
                                    DiscoveredCht(
                                        id = "local:${child.absolutePath}",
                                        name = child.name,
                                        source = ChtSource.EXTERNAL_FILE,
                                        content = bytes,
                                    ),
                                )
                            }
                        }
                    }
                }
            }.onFailure {
                Timber.e(it, "Failed to scan local file .cht files for game ${game.id}")
            }
        }

        return results.distinctBy { it.id }
    }

    suspend fun syncDiscoveredCheats(
        context: Context,
        game: Game,
        cheatDao: CheatDao,
    ): Int {
        if (!CheatSupport.isSystemSupported(game.systemId)) {
            return 0
        }

        val discovered = discoverChtFiles(context, game)
        if (discovered.isEmpty()) {
            return 0
        }

        val digest = MessageDigest.getInstance("SHA-256")
        discovered.sortedBy { it.id }.forEach { cht ->
            digest.update(cht.id.toByteArray(Charsets.UTF_8))
            digest.update(cht.content)
        }
        val currentHash = digest.digest().joinToString("") { "%02x".format(it) }

        val prefs = SharedPreferencesHelper.getSharedPreferences(context)
        val prefKey = "$PREFS_AUTO_CHT_HASH_PREFIX${game.id}"
        val savedHash = prefs.getString(prefKey, null)
        if (savedHash == currentHash) {
            return 0
        }

        val existingCheats = cheatDao.getCheatsForGameSync(game.id)
        val existingNormalizedCodes = existingCheats.map { ChtImportManager.normalizeCode(it.code) }.toMutableSet()
        val seenCodesInBatch = mutableSetOf<String>()

        val newCheatsToInsert = mutableListOf<Cheat>()
        var currentOrderIndex = existingCheats.size

        for (discoveredFile in discovered) {
            val chtFile = runCatching {
                ChtParser.parse(ByteArrayInputStream(discoveredFile.content))
            }.getOrNull() ?: continue

            for (entry in chtFile.entries) {
                val formattedCode = entry.codeWithNewlines
                if (formattedCode.isEmpty()) continue

                val normalized = ChtImportManager.normalizeCode(formattedCode)
                if (existingNormalizedCodes.contains(normalized) || seenCodesInBatch.contains(normalized)) {
                    continue
                }

                val detectedType = CheatValidator.detectType(game.systemId, formattedCode) ?: continue
                val validation = CheatValidator.validate(detectedType, formattedCode)
                if (validation !is ValidationResult.Valid) continue

                val title = entry.description.trim().ifEmpty { "Cheat ${entry.index}" }
                val isMaster = MASTER_CODE_REGEX.containsMatchIn(title)

                seenCodesInBatch.add(normalized)
                newCheatsToInsert.add(
                    Cheat(
                        gameId = game.id,
                        title = title,
                        description = context.getString(com.swordfish.lemuroid.lib.R.string.cheat_auto_discovered_desc, discoveredFile.name),
                        systemId = game.systemId,
                        cheatType = detectedType.id,
                        code = formattedCode,
                        enabled = false,
                        isMasterCode = isMaster,
                        masterCodeId = null,
                        orderIndex = currentOrderIndex++,
                    ),
                )
            }
        }

        if (newCheatsToInsert.isNotEmpty()) {
            cheatDao.insertCheats(newCheatsToInsert)
            Timber.i("Auto-discovered and imported ${newCheatsToInsert.size} cheats for game ${game.id} (${game.title})")
        }

        prefs.edit().putString(prefKey, currentHash).apply()
        return newCheatsToInsert.size
    }

    fun clearAutoChtHash(context: Context, gameId: Int) {
        val prefs = SharedPreferencesHelper.getSharedPreferences(context)
        prefs.edit().remove("$PREFS_AUTO_CHT_HASH_PREFIX$gameId").apply()
    }

    private fun getExternalFolders(context: Context): List<String> {
        val prefString = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)
        val preferenceManager = SharedPreferencesHelper.getLegacySharedPreferences(context)
        return preferenceManager.getStringSet(prefString, null)?.toList() ?: emptyList()
    }
}
