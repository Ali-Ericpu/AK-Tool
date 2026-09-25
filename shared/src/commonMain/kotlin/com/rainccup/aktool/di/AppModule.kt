package com.rainccup.aktool.di

import com.rainccup.aktool.core.data.datasource.ConfigRepository
import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.data.repository.ConfigSource
import com.rainccup.aktool.core.domain.usecase.admin.AddFlushMessageUseCase
import com.rainccup.aktool.core.domain.usecase.admin.GainItemUseCase
import com.rainccup.aktool.core.domain.usecase.admin.QueryAccountByUidUseCase
import com.rainccup.aktool.core.domain.usecase.admin.RegisterAccountUseCase
import com.rainccup.aktool.core.domain.usecase.admin.ResetActivityUseCase
import com.rainccup.aktool.core.domain.usecase.admin.ResetIntegratedStrategiesUseCase
import com.rainccup.aktool.core.domain.usecase.admin.SyncValidCodeUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllCharactersUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllFlagsUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllStagesUseCase
import com.rainccup.aktool.core.domain.usecase.character.ChangeEvolvePhaseUseCase
import com.rainccup.aktool.core.domain.usecase.character.GainCharacterUseCase
import com.rainccup.aktool.core.domain.usecase.character.GetCharacterLimitsUseCase
import com.rainccup.aktool.core.domain.usecase.character.LoadCharactersUseCase
import com.rainccup.aktool.core.domain.usecase.character.SaveCharacterUseCase
import com.rainccup.aktool.core.domain.usecase.config.GetConfigUseCase
import com.rainccup.aktool.core.domain.usecase.config.SaveConfigUseCase
import com.rainccup.aktool.core.domain.usecase.config.UpdateGameTableUseCase
import com.rainccup.aktool.core.domain.usecase.status.GetStatusUseCase
import com.rainccup.aktool.core.domain.usecase.status.SaveStatusUseCase
import com.rainccup.aktool.core.message.BusMessenger
import com.rainccup.aktool.core.message.MessageBus
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.core.network.NetworkConfig
import com.rainccup.aktool.core.platform.AppPaths
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.FilePicker
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.platform.PlatformTheme
import com.rainccup.aktool.core.platform.createAppPaths
import com.rainccup.aktool.core.platform.createClipboard
import com.rainccup.aktool.core.platform.createFilePicker
import com.rainccup.aktool.core.platform.createPlatformTheme
import org.koin.dsl.module

val appModule = module {
    single<AppPaths> { createAppPaths() }
    single { MessageBus() }
    single<Messenger> { BusMessenger(get()) }
    single<ClipboardPort> { createClipboard() }
    single<FilePicker> { createFilePicker() }
    single<PlatformTheme> { createPlatformTheme() }

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
    single { GameTableRepository(get(), get()) }
    single { AdminRepository(get(), get(), get()) }

    // Use cases（无状态，用 factory）
    factory { LoadCharactersUseCase(get(), get()) }
    factory { SaveCharacterUseCase(get(), get()) }
    factory { GainCharacterUseCase(get()) }
    factory { ChangeEvolvePhaseUseCase(get()) }
    factory { GetCharacterLimitsUseCase(get()) }

    factory { UnlockAllCharactersUseCase(get()) }
    factory { UnlockAllStagesUseCase(get()) }
    factory { UnlockAllFlagsUseCase(get()) }
    factory { AddFlushMessageUseCase(get()) }
    factory { GainItemUseCase(get()) }
    factory { ResetActivityUseCase(get()) }
    factory { RegisterAccountUseCase(get()) }
    factory { ResetIntegratedStrategiesUseCase(get()) }
    factory { SyncValidCodeUseCase(get()) }
    factory { QueryAccountByUidUseCase(get()) }

    factory { GetStatusUseCase(get()) }
    factory { SaveStatusUseCase(get()) }

    factory { GetConfigUseCase(get()) }
    factory { SaveConfigUseCase(get(), get()) }
    factory { UpdateGameTableUseCase(get()) }
}
