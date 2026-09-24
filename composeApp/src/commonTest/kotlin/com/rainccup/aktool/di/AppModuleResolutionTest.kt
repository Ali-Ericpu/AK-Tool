package com.rainccup.aktool.di

import com.rainccup.aktool.core.datastore.ConfigRepository
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.core.network.NetworkConfig
import com.rainccup.aktool.core.platform.createAppPaths
import com.rainccup.aktool.core.platform.createClipboard
import com.rainccup.aktool.core.platform.createFilePicker
import com.rainccup.aktool.core.platform.createMessenger
import com.rainccup.aktool.core.platform.createPlatformTheme
import com.rainccup.aktool.core.repository.AdminRepository
import com.rainccup.aktool.core.repository.ConfigSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppModuleResolutionTest : KoinTest {

    @BeforeTest
    fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun createsAdminRepository() {
        assertNotNull(getKoin().get<AdminRepository>())
    }

    @Test
    fun createsConfigSourceBinding() {
        val impl = getKoin().get<ConfigRepository>()
        val source = getKoin().get<ConfigSource>()
        assertTrue(source === impl, "ConfigSource must resolve to ConfigRepository instance")
    }

    @Test
    fun createsDataAndNetworkDeps() {
        assertNotNull(getKoin().get<ApiClient>())
        assertNotNull(getKoin().get<GameTableRepository>())
        assertNotNull(getKoin().get<HttpClientProvider>())
        assertNotNull(getKoin().get<NetworkConfig>())
    }
}
