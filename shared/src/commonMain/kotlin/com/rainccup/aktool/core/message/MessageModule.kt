package com.rainccup.aktool.core.message

import org.koin.dsl.module

val messageModule = module {
    single { MessageBus() }
    single<com.rainccup.aktool.core.platform.Messenger> { BusMessenger(get()) }
}
