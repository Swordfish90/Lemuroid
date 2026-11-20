package com.swordfish.lemuroid.lib.cheats.cht

import java.io.InputStream
import java.nio.charset.Charset

object ChtParser {
    private val CHEAT_PROPERTY_REGEX = Regex("""^cheat(\d+)_(.+)$""", RegexOption.IGNORE_CASE)

    fun parse(content: String): ChtFile {
        val cleanContent = if (content.startsWith("\uFEFF")) content.substring(1) else content
        val lines = cleanContent.lines()

        var declaredCount: Int? = null
        val entriesByIndex = mutableMapOf<Int, MutableMap<String, String>>()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";") || line.startsWith("//")) {
                continue
            }

            val eqIndex = line.indexOf('=')
            if (eqIndex == -1) continue

            val rawKey = line.substring(0, eqIndex).trim()
            val rawValue = line.substring(eqIndex + 1).trim()
            val value = unquote(rawValue)

            if (rawKey.equals("cheats", ignoreCase = true)) {
                declaredCount = value.toIntOrNull()
                continue
            }

            val match = CHEAT_PROPERTY_REGEX.matchEntire(rawKey)
            if (match != null) {
                val index = match.groupValues[1].toIntOrNull() ?: continue
                val prop = match.groupValues[2].lowercase()
                entriesByIndex.getOrPut(index) { mutableMapOf() }[prop] = value
            }
        }

        val entries =
            entriesByIndex.keys.sorted().mapNotNull { idx ->
                val props = entriesByIndex[idx] ?: return@mapNotNull null
                val desc = props["desc"] ?: "Cheat $idx"
                val code = props["code"] ?: ""
                val enableStr = props["enable"]?.lowercase() ?: "false"
                val enabled = enableStr == "true" || enableStr == "1"
                val handler = props["handler"]?.toIntOrNull()
                val memorySearchSize = props["memory_search_size"]?.toIntOrNull()
                val cheatType = props["cheat_type"]?.toIntOrNull()
                val repeatCount = props["repeat_count"]?.toIntOrNull()
                val repeatAddToValue = props["repeat_add_to_value"]?.toIntOrNull()
                val repeatAddToAddress = props["repeat_add_to_address"]?.toIntOrNull()

                val knownProps =
                    setOf(
                        "desc",
                        "code",
                        "enable",
                        "handler",
                        "memory_search_size",
                        "cheat_type",
                        "repeat_count",
                        "repeat_add_to_value",
                        "repeat_add_to_address",
                    )
                val extras = props.filterKeys { it !in knownProps }

                ChtEntry(
                    index = idx,
                    description = desc,
                    code = code,
                    enabled = enabled,
                    handler = handler,
                    memorySearchSize = memorySearchSize,
                    cheatType = cheatType,
                    repeatCount = repeatCount,
                    repeatAddToValue = repeatAddToValue,
                    repeatAddToAddress = repeatAddToAddress,
                    extraFields = extras,
                )
            }

        return ChtFile(
            cheatCountHeader = declaredCount,
            entries = entries,
        )
    }

    fun parse(inputStream: InputStream): ChtFile {
        val bytes = inputStream.readBytes()
        val text =
            try {
                bytes.toString(Charsets.UTF_8)
            } catch (e: Exception) {
                bytes.toString(Charset.forName("ISO-8859-1"))
            }
        return parse(text)
    }

    private fun unquote(rawValue: String): String {
        var s = rawValue.trim()
        if (s.startsWith("\"")) {
            val endQuoteIndex = s.lastIndexOf("\"")
            if (endQuoteIndex > 0) {
                return s.substring(1, endQuoteIndex)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            }
        }
        val commentIdx = s.indexOfFirst { it == '#' || it == ';' }
        if (commentIdx != -1) {
            s = s.substring(0, commentIdx).trim()
        }
        return s
    }
}
