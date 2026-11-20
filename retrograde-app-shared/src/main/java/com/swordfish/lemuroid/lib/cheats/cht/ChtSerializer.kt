package com.swordfish.lemuroid.lib.cheats.cht

import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import java.io.OutputStream

object ChtSerializer {
    fun serialize(cheats: List<Cheat>): String {
        val sb = StringBuilder()
        sb.append("cheats = ${cheats.size}\n\n")

        for ((idx, cheat) in cheats.withIndex()) {
            val escapedDesc = cheat.title.replace("\\", "\\\\").replace("\"", "\\\"")
            val chtCode =
                cheat.code
                    .split(Regex("[\\r\\n+]+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("+")

            sb.append("cheat${idx}_desc = \"$escapedDesc\"\n")
            sb.append("cheat${idx}_code = \"$chtCode\"\n")
            sb.append("cheat${idx}_enable = ${cheat.enabled}\n\n")
        }

        return sb.toString()
    }

    fun serialize(
        outputStream: OutputStream,
        cheats: List<Cheat>,
    ) {
        val content = serialize(cheats)
        outputStream.write(content.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    fun serializeEntries(entries: List<ChtEntry>): String {
        val sb = StringBuilder()
        sb.append("cheats = ${entries.size}\n\n")

        for ((idx, entry) in entries.withIndex()) {
            val escapedDesc = entry.description.replace("\\", "\\\\").replace("\"", "\\\"")
            val chtCode =
                entry.code
                    .split(Regex("[\\r\\n+]+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("+")

            sb.append("cheat${idx}_desc = \"$escapedDesc\"\n")
            sb.append("cheat${idx}_code = \"$chtCode\"\n")
            sb.append("cheat${idx}_enable = ${entry.enabled}\n")
            if (entry.handler != null) {
                sb.append("cheat${idx}_handler = ${entry.handler}\n")
            }
            if (entry.memorySearchSize != null) {
                sb.append("cheat${idx}_memory_search_size = ${entry.memorySearchSize}\n")
            }
            if (entry.cheatType != null) {
                sb.append("cheat${idx}_cheat_type = ${entry.cheatType}\n")
            }
            if (entry.repeatCount != null) {
                sb.append("cheat${idx}_repeat_count = ${entry.repeatCount}\n")
            }
            if (entry.repeatAddToValue != null) {
                sb.append("cheat${idx}_repeat_add_to_value = ${entry.repeatAddToValue}\n")
            }
            if (entry.repeatAddToAddress != null) {
                sb.append("cheat${idx}_repeat_add_to_address = ${entry.repeatAddToAddress}\n")
            }
            for ((k, v) in entry.extraFields) {
                sb.append("cheat${idx}_$k = \"$v\"\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    fun serializeEntries(
        outputStream: OutputStream,
        entries: List<ChtEntry>,
    ) {
        val content = serializeEntries(entries)
        outputStream.write(content.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }
}
