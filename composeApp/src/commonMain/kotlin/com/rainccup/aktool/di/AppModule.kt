package com.rainccup.aktool.di

import com.rainccup.aktool.core.datastore.ConfigRepository
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.core.network.NetworkConfig
import com.rainccup.aktool.core.platform.AppPaths
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.FilePicker
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.platform.PlatformTheme
import com.rainccup.aktool.core.platform.createClipboard
import com.rainccup.aktool.core.platform.createFilePicker
import com.rainccup.aktool.core.platform.createMessenger
import com.rainccup.aktool.core.platform.createAppPaths
import com.rainccup.aktool.core.platform.createPlatformTheme
import com.rainccup.aktool.core.repository.AdminRepository
import org.koin.dsl.module

val appModule = module {
    single<AppPaths> { createAppPaths() }
    single<Messenger> { createMessenger() }
    single<ClipboardPort> { createClipboard() }
    single<FilePicker> { createFilePicker() }
    single<PlatformTheme> { createPlatformTheme() }

    single { NetworkConfig() }
    single { HttpClientProvider(get()) }
    single { ApiClient(get<NetworkConfig>()) { get<HttpClientProvider>().client() } }

    single { ConfigRepository(get()) }
    single { GameTableRepository(get(), get()) }
    single { AdminRepository(get(), get(), get()) }
}
