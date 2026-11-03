package come.asg.lemudroid.common.kotlin

import java.security.MessageDigest

/** Stable, filesystem-safe identity derived from a game's source identity. */
fun stableGameIdentity(vararg parts: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(parts.joinToString("\u0000").toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
        .take(16)
