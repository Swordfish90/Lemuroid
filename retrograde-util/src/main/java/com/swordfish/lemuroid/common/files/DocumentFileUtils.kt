package com.swordfish.lemuroid.common.files

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

fun DocumentFile.readLines(context: Context, charset: Charset = Charsets.UTF_8): List<String> {
    val result = mutableListOf<String>()
    forEachLine(context, charset) { result.add(it) }
    return result
}

private fun DocumentFile.forEachLine(context: Context, charset: Charset = Charsets.UTF_8, action: (line: String) -> Unit): Unit {
    // Note: close is called at forEachLine
    val inputStream = context.contentResolver.openInputStream(this.uri)
    BufferedReader(InputStreamReader(inputStream, charset)).forEachLine(action)
}
