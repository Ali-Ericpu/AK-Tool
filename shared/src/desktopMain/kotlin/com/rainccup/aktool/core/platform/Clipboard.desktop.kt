package com.rainccup.aktool.core.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class DesktopClipboard : ClipboardPort {
    override fun setText(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}

actual fun createClipboard(): ClipboardPort = DesktopClipboard()
