package com.rainccup.aktool.core.datastore

import java.io.File

actual fun fileExists(path: String): Boolean = File(path).exists()

actual fun readFileText(path: String): String? =
    File(path).takeIf { it.exists() }?.readText()

actual fun writeFileText(path: String, text: String) {
    val f = File(path)
    f.parentFile?.mkdirs()
    f.writeText(text)
}

actual fun mkdirs(path: String) {
    File(path).mkdirs()
}
