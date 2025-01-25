package com.swordfish.lemuroid.app.shared.savesync


import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import androidx.annotation.StringRes
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import java.io.File

/*
This is heavily based on two sources:
 1. https://developer.android.com/guide/topics/providers/create-document-provider#kotlin
 2. https://github.com/dolphin-emu/dolphin/blob/68fe6779eb8c9a1594cb8975b3e9edbbd428c405/Source/Android/app/src/main/java/org/dolphinemu/dolphinemu/features/DocumentProvider.kt

and to lesser extend this:
 3. https://android.googlesource.com/platform/packages/providers/DownloadProvider/+/8ec0057/src/com/android/providers/downloads/DownloadStorageProvider.java

 Especially the dolpin-source was extremely helpful with understanding and implementing this feature. It would not have been possible without. Thanks!
 */

class SavegameProvider : DocumentsProvider() {

    private lateinit var directoryManager: DirectoriesManager
    companion object {
        const val ROOT_ID = "internal_data"

        private val DEFAULT_ROOT_PROJECTION = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID
        )

        private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
        )
    }

    override fun onCreate(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            directoryManager = DirectoriesManager(requireContext())
        } else {
            val manager = this.context?.let { DirectoriesManager(it) }
            if(manager == null) {
                return false
            }
            directoryManager = manager
        }
        return true
    }


    override fun queryRoots(projection: Array<String>?): Cursor {

        val result = MatrixCursor(resolveRootProjection(projection))
        result.newRow().apply {
            add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID)
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_ID)
            add(DocumentsContract.Root.COLUMN_TITLE, getString(com.swordfish.lemuroid.R.string.lemuroid_name))
            add(DocumentsContract.Root.COLUMN_ICON, com.swordfish.lemuroid.R.mipmap.lemuroid_launcher)
            add(
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_SUPPORTS_CREATE or
                        DocumentsContract.Root.FLAG_SUPPORTS_SEARCH
            )
        }
        return result
    }

    override fun queryDocument(documentId: String, projection: Array<String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        addIdToCursor(documentId, cursor)
        return cursor
    }

    override fun queryChildDocuments(parentDocumentId: String, projection: Array<String>?, queryArgs: String?): Cursor {
        return MatrixCursor(resolveDocumentProjection(projection)).apply {
            resolveId(parentDocumentId).listFiles()?.forEach { file ->
                addFileToCursor(file, this)
            }
        }
    }

    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor? {
        val file = resolveId(documentId)
        if (!file.canWrite() && (mode == "rw" || mode == "w" || mode == "a" || mode == "t")) {
            throw UnsupportedOperationException("File $documentId is not writeable!")
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode))
    }

    override fun openDocumentThumbnail(documentId: String, sizeHint: android.graphics.Point, signal: CancellationSignal): AssetFileDescriptor {
        val descriptor = openDocument(documentId, "r", signal)
        return AssetFileDescriptor(descriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
    }

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        val parent = resolveId(parentDocumentId)

        if(mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
            File(parent, displayName).mkdirs()
        } else {
            File(parent, displayName).createNewFile()
        }
        return "$parentDocumentId/$displayName"
    }

    override fun deleteDocument(documentId: String) {
        resolveId(documentId).delete()
    }

    override fun renameDocument(documentId: String, displayName: String): String? {
        val file = resolveId(documentId)
        val new = File(file.parent, displayName)
        file.renameTo(new)
        return null
    }


    /* https://android.googlesource.com/platform/packages/providers/DownloadProvider/+/8ec0057/src/com/android/providers/downloads/DownloadStorageProvider.java */
    private fun resolveRootProjection(projection: Array<String>?): Array<String> {
        return projection ?: DEFAULT_ROOT_PROJECTION
    }

    private fun resolveDocumentProjection(projection: Array<String>?): Array<String> {
        return projection ?: DEFAULT_DOCUMENT_PROJECTION
    }


    private fun addIdToCursor(documentId: String, cursor: MatrixCursor) {
        addFileToCursor(resolveId(documentId), cursor)
    }

    private fun addFileToCursor(file: File, cursor: MatrixCursor) {

        if(file == directoryManager.getInternalRomsDirectory()) {
            return
        }

        val name = determineName(file)
        val flags = determineFlags(file)

        cursor.newRow().apply {
            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, generateId(file))
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, getType(file))
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, name)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified())
            add(DocumentsContract.Document.COLUMN_FLAGS, flags)
            add(DocumentsContract.Document.COLUMN_SIZE, file.length())
        }
    }

    private fun determineFlags(file: File): Int {
        var flags = 0

        if (file.canWrite()) {
            flags = flags or DocumentsContract.Document.FLAG_SUPPORTS_WRITE
            flags = flags or DocumentsContract.Document.FLAG_SUPPORTS_RENAME
            flags = flags or DocumentsContract.Document.FLAG_SUPPORTS_DELETE
        }

        if(file.isDirectory) {
            flags = flags or DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE
        }

        return flags
    }

    private fun determineName(file: File): String {
        if (file == directoryManager.getStatesDirectory()) {
            return getString(com.swordfish.lemuroid.R.string.documentprovider_folder_override_states)
        }
        if (file == directoryManager.getSavesDirectory()) {
            return getString(com.swordfish.lemuroid.R.string.documentprovider_folder_override_saves)
        }
        if (file == directoryManager.getInternalRomsDirectory()) {
            return getString(com.swordfish.lemuroid.R.string.documentprovider_folder_override_roms)
        }
        if (file == directoryManager.getStatesPreviewDirectory()) {
            return getString(com.swordfish.lemuroid.R.string.documentprovider_folder_override_previews)
        }

        if (isDirectChild(file, directoryManager.getSavesDirectory())) {
            when(file.name) {
                "mgba" -> return "Game Boy Advance"
                "fbneo" -> return "FinalBurn Neo"
                "gb" -> return "Game Boy"
                "gbc" -> return "Game Boy Color"
                "n64" -> return "Nintendo 64"
                "nds" -> return "Nintendo DS"
                "nes" -> return "Nintendo Entertainment System"
                "snes" -> return "Super Nintendo Entertainment System"
                "psx" -> return "Playsation 1"
            }
        }

        return file.name
    }

    private fun getString(@StringRes id: Int): String {
        return context!!.getString(id)
    }

    private fun isDirectChild(file: File, of: File): Boolean {
        of.listFiles()?.forEach {
            if(it == file) {
                return true
            }
        }
        return false
    }

    /* Helper */

    private fun generateId(file: File): String {
        return ROOT_ID + "/" + file.toRelativeString(getRoot())
    }

    private fun resolveId(id: String): File {
        if (id == ROOT_ID) {
            return getRoot()
        }
        val localId = id.removePrefix("$ROOT_ID/")
        val file = getRoot().resolve(localId)
        return file
    }

    private fun getRoot(): File = directoryManager.getBaseDir()

    private fun getType(file: File): String {
        val mime = MimeTypeMap.getSingleton()
        var type = "application/octet-stream"

        if(file.isDirectory){
            type = DocumentsContract.Document.MIME_TYPE_DIR
        } else if (mime.hasMimeType(file.extension)) {
            type = mime.getMimeTypeFromExtension(file.extension).toString()
        }

        return type
    }
}
