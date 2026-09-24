package com.rainccup.aktool.core.platform

import android.widget.Toast

class AndroidMessenger : Messenger {
    override fun show(message: String) {
        Toast.makeText(AndroidContextHolder.appContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSuccess() = show("保存成功")
}

actual fun createMessenger(): Messenger = AndroidMessenger()
