package com.rainccup.aktool.core.platform

import co.touchlab.kermit.Logger

class DesktopMessenger : Messenger {
    override fun show(message: String) {
        Logger.i("Messenger") { message }
    }

    override fun showSuccess() = show("保存成功")
}

actual fun createMessenger(): Messenger = DesktopMessenger()
