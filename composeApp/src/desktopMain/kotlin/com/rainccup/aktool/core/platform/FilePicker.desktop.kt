package com.rainccup.aktool.core.platform

import java.awt.FileDialog
import java.awt.Frame

class DesktopFilePicker : FilePicker {
    override fun pickImage(onResult: (String?) -> Unit) = pick(onResult)

    override fun pickFile(onResult: (String?) -> Unit) = pick(onResult)

    private fun pick(onResult: (String?) -> Unit) {
        val dialog = FileDialog(null as Frame?, "选择文件", FileDialog.LOAD)
        dialog.isVisible = true
        val file = dialog.file?.let { dialog.directory + it }
        onResult(file)
    }
}

actual fun createFilePicker(): FilePicker = DesktopFilePicker()
