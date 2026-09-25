package com.rainccup.aktool.core.data

import com.rainccup.aktool.core.data.datasource.ConfigRepository
import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.data.repository.ConfigSource
import com.rainccup.aktool.core.data.repository.ConfigStore
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.core.network.NetworkConfig
import org.koin.dsl.module

val dataModule = module {
    // Apply the persisted server settings at startup so Ktor does not fall back to the
    // hardcoded default until the user re-saves the settings screen.
    single {
        val saved = get<ConfigRepository>().current()
        NetworkConfig(
            baseUrl = saved.serverUri.ifBlank { NetworkConfig.DEFAULT_BASE_URL },
            uid = saved.uid,
            adminKey = saved.adminKey,
        )
    }
    single { HttpClientProvider(get()) }
    single { ApiClient(get<NetworkConfig>()) { get<HttpClientProvider>().client() } }

    single { ConfigRepository(get()) }
    single<ConfigSource> { get<ConfigRepository>() }
    single { ConfigStore(get(), get()) }
    single { GameTableRepository(get(), get()) }
    single { AdminRepository(get(), get(), get()) }
}
