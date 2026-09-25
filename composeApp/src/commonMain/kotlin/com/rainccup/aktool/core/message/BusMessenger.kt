package com.rainccup.aktool.core.message

import com.rainccup.aktool.core.platform.Messenger

class BusMessenger(private val bus: MessageBus) : Messenger {
    override fun show(message: String) = bus.publish(message)

    override fun showSuccess() = show("保存成功")
}
