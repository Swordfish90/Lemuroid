package com.swordfish.lemuroid.common.files

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

fun InputStream.readLines(charset: Charset = Charsets.UTF_8): List<String> {
    val result = mutableListOf<String>()
    forEachLine(charset) { result.add(it) }
    return result
}

private fun InputStream.forEachLine(
    charset: Charset = Charsets.UTF_8,
    action: (line: String) -> Unit,
) {
    BufferedReader(InputStreamReader(this, charset)).use { it.forEachLine(action) }
}
