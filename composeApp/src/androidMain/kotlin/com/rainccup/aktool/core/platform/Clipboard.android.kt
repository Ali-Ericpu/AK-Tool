package com.rainccup.aktool.core.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class AndroidClipboard : ClipboardPort {
    override fun setText(text: String) {
        val cm =
            AndroidContextHolder.appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("aktool", text))
    }
}

actual fun createClipboard(): ClipboardPort = AndroidClipboard()
