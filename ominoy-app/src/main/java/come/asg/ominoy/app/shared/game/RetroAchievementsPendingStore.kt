package come.asg.ominoy.app.shared.game

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Durable, encrypted queue of locally detected RetroAchievements unlocks. */
object RetroAchievementsPendingStore {
    enum class State { PENDING, FAILED }

    data class PendingUnlock(
        val username: String,
        val achievementId: Long,
        val rawRomMd5: String,
        val raGameId: Int,
        val hardcore: Boolean,
        val unlockTimeMillis: Long,
        val localLibraryGameId: Int,
        val gameTitle: String,
        val attemptCount: Int = 0,
        val lastError: String = "",
        val state: State = State.PENDING,
    ) {
        val key: String get() = "${username.lowercase()}|$raGameId|$rawRomMd5|$achievementId|${if (hardcore) 1 else 0}"
    }

    private const val FILE_NAME = "retro-achievements-pending.bin"
    private const val KEY_ALIAS = "ominoy.ra.pending.v1"
    private const val MAX_ENTRIES = 2_000
    private const val IV_LENGTH = 12
    private val lock = Any()

    private fun file(context: Context) = File(context.applicationContext.filesDir, FILE_NAME)

    fun enqueue(context: Context, unlock: PendingUnlock) = synchronized(lock) {
        val values = readLocked(context).toMutableList()
        val index = values.indexOfFirst { it.key == unlock.key }
        if (index >= 0) {
            val existing = values[index]
            values[index] = existing.copy(
                rawRomMd5 = existing.rawRomMd5.ifBlank { unlock.rawRomMd5 },
                raGameId = existing.raGameId.takeIf { it > 0 } ?: unlock.raGameId,
                localLibraryGameId = existing.localLibraryGameId.takeIf { it > 0 } ?: unlock.localLibraryGameId,
                gameTitle = existing.gameTitle.ifBlank { unlock.gameTitle },
                unlockTimeMillis = minOf(existing.unlockTimeMillis, unlock.unlockTimeMillis),
                state = if (existing.state == State.FAILED) State.PENDING else existing.state,
            )
        } else {
            values += unlock
        }
        writeLocked(context, values.takeLast(MAX_ENTRIES))
    }

    fun snapshot(context: Context): List<PendingUnlock> = synchronized(lock) { readLocked(context) }

    fun update(context: Context, key: String, transform: (PendingUnlock) -> PendingUnlock) = synchronized(lock) {
        val values = readLocked(context).toMutableList()
        val index = values.indexOfFirst { it.key == key }
        if (index >= 0) {
            values[index] = transform(values[index])
            writeLocked(context, values)
        }
    }

    fun remove(context: Context, key: String) = synchronized(lock) {
        val values = readLocked(context).filterNot { it.key == key }
        writeLocked(context, values)
    }

    fun pendingCount(context: Context): Int = snapshot(context).count { it.state == State.PENDING }

    private fun readLocked(context: Context): List<PendingUnlock> {
        val target = file(context)
        if (!target.isFile) return emptyList()
        return runCatching {
            val clear = decrypt(target.readBytes()).toString(StandardCharsets.UTF_8)
            val array = JSONArray(clear)
            (0 until array.length()).mapNotNull { index -> array.optJSONObject(index)?.fromJson() }
        }.getOrElse { error ->
            Timber.w(error, "Could not read pending RetroAchievements queue; preserving corrupt file")
            val backup = File(target.parentFile, "${target.name}.corrupt-${System.currentTimeMillis()}")
            runCatching { target.copyTo(backup, overwrite = false); target.delete() }
            emptyList()
        }
    }

    private fun writeLocked(context: Context, values: List<PendingUnlock>) {
        val text = JSONArray().apply { values.forEach { put(it.toJson()) } }.toString()
        val target = file(context)
        val temp = File(target.parentFile, ".${target.name}.tmp")
        runCatching {
            target.parentFile?.mkdirs()
            temp.writeBytes(encrypt(text.toByteArray(StandardCharsets.UTF_8)))
            if (!temp.renameTo(target)) throw IllegalStateException("Could not atomically replace pending queue")
        }.onFailure { error -> Timber.e(error, "Could not persist pending RetroAchievements queue") }
    }

    private fun PendingUnlock.toJson() = JSONObject().apply {
        put("username", username)
        put("achievementId", achievementId)
        put("rawRomMd5", rawRomMd5)
        put("raGameId", raGameId)
        put("hardcore", hardcore)
        put("unlockTimeMillis", unlockTimeMillis)
        put("localLibraryGameId", localLibraryGameId)
        put("gameTitle", gameTitle)
        put("attemptCount", attemptCount)
        put("lastError", lastError)
        put("state", state.name)
    }

    private fun JSONObject.fromJson() = PendingUnlock(
        username = optString("username"),
        achievementId = optLong("achievementId"),
        rawRomMd5 = optString("rawRomMd5"),
        raGameId = optInt("raGameId"),
        hardcore = optBoolean("hardcore"),
        unlockTimeMillis = optLong("unlockTimeMillis"),
        localLibraryGameId = optInt("localLibraryGameId"),
        gameTitle = optString("gameTitle"),
        attemptCount = optInt("attemptCount"),
        lastError = optString("lastError"),
        state = runCatching { State.valueOf(optString("state", State.PENDING.name)) }.getOrDefault(State.PENDING),
    )

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build())
        return generator.generateKey()
    }

    private fun encrypt(clear: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(clear)
    }

    private fun decrypt(value: ByteArray): ByteArray {
        require(value.size > IV_LENGTH)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, value.copyOfRange(0, IV_LENGTH)))
        return cipher.doFinal(value.copyOfRange(IV_LENGTH, value.size))
    }

    fun md5(value: String): String = MessageDigest.getInstance("MD5")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
