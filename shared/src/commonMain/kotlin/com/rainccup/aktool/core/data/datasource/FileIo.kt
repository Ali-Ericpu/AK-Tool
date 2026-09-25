package com.rainccup.aktool.core.data.datasource

expect fun fileExists(path: String): Boolean
expect fun readFileText(path: String): String?
expect fun writeFileText(path: String, text: String)
expect fun mkdirs(path: String)
