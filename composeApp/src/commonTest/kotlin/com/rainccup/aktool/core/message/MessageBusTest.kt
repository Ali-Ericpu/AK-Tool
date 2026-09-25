package com.rainccup.aktool.core.message

import com.rainccup.aktool.core.platform.Messenger
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageBusTest {

    @Test
    fun publish_deliversMessageToCollector() = runTest {
        val bus = MessageBus()
        val pending = async { bus.messages.first() }
        yield()
        bus.publish("保存成功")
        assertEquals("保存成功", pending.await())
    }

    @Test
    fun busMessenger_showSuccess_publishesCorrectText() = runTest {
        val bus = MessageBus()
        val messenger: Messenger = BusMessenger(bus)
        val pending = async { bus.messages.first() }
        yield()
        messenger.showSuccess()
        assertEquals("保存成功", pending.await())
    }
}
