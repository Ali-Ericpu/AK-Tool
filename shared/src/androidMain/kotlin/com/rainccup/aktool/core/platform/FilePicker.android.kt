package com.rainccup.aktool.core.platform

object AndroidFilePickerBridge {
    var pickImageImpl: (((String?) -> Unit) -> Unit)? = null
    var pickFileImpl: (((String?) -> Unit) -> Unit)? = null
}

class AndroidFilePicker : FilePicker {
    override fun pickImage(onResult: (String?) -> Unit) {
        AndroidFilePickerBridge.pickImageImpl?.invoke(onResult) ?: onResult(null)
    }

    override fun pickFile(onResult: (String?) -> Unit) {
        AndroidFilePickerBridge.pickFileImpl?.invoke(onResult) ?: onResult(null)
    }
}

actual fun createFilePicker(): FilePicker = AndroidFilePicker()
