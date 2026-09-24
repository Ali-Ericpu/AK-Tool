package com.rainccup.aktool.core.platform

interface AppPaths {
    val configDir: String
    val dataDir: String
    fun resolve(relativePath: String): String
}

interface Messenger {
    fun show(message: String)
    fun showSuccess()
}

interface ClipboardPort {
    fun setText(text: String)
}

interface FilePicker {
    fun pickImage(onResult: (String?) -> Unit)
    fun pickFile(onResult: (String?) -> Unit)
}

interface PlatformTheme {
    fun supportsDynamicColor(): Boolean
}
