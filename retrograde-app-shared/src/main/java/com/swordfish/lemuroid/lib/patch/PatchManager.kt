package com.swordfish.lemuroid.lib.patch

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

object PatchManager {
    private const val PREFS_PATCHES_PREFIX = "pref_key_game_patches_"

    fun getEnabledPatchIds(
        context: Context,
        gameId: Int,
        availablePatches: List<RomPatch> = emptyList(),
    ): Set<String> {
        val prefs = SharedPreferencesHelper.getSharedPreferences(context)
        val key = "$PREFS_PATCHES_PREFIX$gameId"
        if (prefs.contains(key)) {
            return prefs.getStringSet(key, emptySet()) ?: emptySet()
        }
        return emptySet()
    }

    fun setEnabledPatchIds(context: Context, gameId: Int, enabledIds: Set<String>) {
        val prefs = SharedPreferencesHelper.getSharedPreferences(context)
        prefs.edit().putStringSet("$PREFS_PATCHES_PREFIX$gameId", enabledIds).apply()
    }

    fun disablePatch(context: Context, gameId: Int, patchId: String) {
        val current = getEnabledPatchIds(context, gameId).toMutableSet()
        if (current.remove(patchId)) {
            setEnabledPatchIds(context, gameId, current)
        }
    }

    fun discoverPatches(context: Context, game: Game): List<RomPatch> {
        val patches = mutableListOf<RomPatch>()
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
                            if (!entry.isDirectory && entry.name.endsWith(".ips", ignoreCase = true)) {
                                val patchBytes = zip.readBytes()
                                val isValid = IpsPatcher.isValidIps(patchBytes)
                                val simpleName = File(entry.name).name
                                patches.add(
                                    RomPatch(
                                        id = "zip:${entry.name}",
                                        name = simpleName,
                                        source = PatchSource.IN_ZIP,
                                        location = entry.name,
                                        sizeBytes = if (entry.size > 0) entry.size else patchBytes.size.toLong(),
                                        isValid = isValid,
                                        validationError = if (!isValid) context.getString(com.swordfish.lemuroid.lib.R.string.game_patch_invalid_header) else null,
                                    ),
                                )
                            }
                            entry = zip.nextEntry
                        }
                    }
                }
            }.onFailure {
                Timber.e(it, "Failed to scan in-zip patches for game ${game.id}")
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
                                val childSize = cursor.getLong(2)
                                if (childName != null && childName.endsWith(".ips", ignoreCase = true)) {
                                    val childBase = childName.substringBeforeLast('.')
                                    val matches = childBase.equals(romBaseName, ignoreCase = true) ||
                                            childBase.startsWith(romBaseName, ignoreCase = true) ||
                                            childBase.contains(romBaseName, ignoreCase = true) ||
                                            romBaseName.contains(childBase, ignoreCase = true)
                                    if (matches) {
                                        val childUri = DocumentsContract.buildDocumentUriUsingTree(rootUri, childDocId)
                                        val isValid = runCatching {
                                            context.contentResolver.openInputStream(childUri)?.use { s ->
                                                val header = ByteArray(5)
                                                s.read(header) == 5 && header.contentEquals(byteArrayOf(0x50, 0x41, 0x54, 0x43, 0x48))
                                            } ?: false
                                        }.getOrDefault(false)

                                        patches.add(
                                            RomPatch(
                                                id = "file:$childDocId",
                                                name = childName,
                                                source = PatchSource.EXTERNAL_FILE,
                                                location = childUri.toString(),
                                                sizeBytes = childSize,
                                                isValid = isValid,
                                                validationError = if (!isValid) context.getString(com.swordfish.lemuroid.lib.R.string.game_patch_invalid_header) else null,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to scan external patches for game ${game.id}")
        }

        return patches.distinctBy { it.id }
    }

    fun loadPatchBytes(context: Context, game: Game, patch: RomPatch): ByteArray? {
        return runCatching {
            when (patch.source) {
                PatchSource.IN_ZIP -> {
                    context.contentResolver.openInputStream(Uri.parse(game.fileUri))?.use { input ->
                        ZipInputStream(input).use { zip ->
                            var entry = zip.nextEntry
                            while (entry != null) {
                                if (entry.name == patch.location) {
                                    return zip.readBytes()
                                }
                                entry = zip.nextEntry
                            }
                        }
                    }
                    null
                }
                PatchSource.EXTERNAL_FILE -> {
                    context.contentResolver.openInputStream(Uri.parse(patch.location))?.use {
                        it.readBytes()
                    }
                }
            }
        }.getOrNull()
    }

    private fun getExternalFolders(context: Context): List<String> {
        val prefString = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)
        val preferenceManager = SharedPreferencesHelper.getLegacySharedPreferences(context)
        return preferenceManager.getStringSet(prefString, null)?.toList() ?: emptyList()
    }
}
