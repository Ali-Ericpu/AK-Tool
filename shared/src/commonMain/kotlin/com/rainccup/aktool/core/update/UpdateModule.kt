package com.rainccup.aktool.core.update

import io.ktor.client.HttpClient
import org.koin.dsl.module

/** 更新链路用自己的客户端（见 [createUpdateHttpClient]），首次用到时才创建。 */
private val updateHttpClient: HttpClient by lazy { createUpdateHttpClient() }

val updateModule = module {
    single { UpdateRepository({ updateHttpClient }, get()) }
    single { UpdateService(get(), get(), get(), get(), get()) }
}
