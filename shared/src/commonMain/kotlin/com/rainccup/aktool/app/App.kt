package com.rainccup.aktool.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import com.rainccup.aktool.core.common.AppConfigContext
import com.rainccup.aktool.core.common.LocalAppConfig
import com.rainccup.aktool.core.data.datasource.ConfigRepository
import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.app.AppNavHost
import com.rainccup.aktool.feature.splash.ui.SplashPage
import com.rainccup.aktool.core.designsystem.theme.AKToolTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun App() {
    val configRepository: ConfigRepository = koinInject()
    val httpClientProvider: HttpClientProvider = koinInject()
    var config by remember { mutableStateOf(configRepository.read()) }
    val coroutineScope = rememberCoroutineScope()

    val darkMode = config.darkMode || isSystemInDarkTheme()
    // spec S2 模式优先级：darkMode > dynamicColor > System
    val colorMode = when {
        config.darkMode -> ColorSchemeMode.Dark
        config.dynamicColor -> ColorSchemeMode.MonetSystem
        else -> ColorSchemeMode.System
    }
    val miuixController = remember(colorMode) { ThemeController(colorMode) }
    MiuixTheme(controller = miuixController) {
        AKToolTheme(darkTheme = darkMode) {
            CompositionLocalProvider(
                LocalAppConfig provides AppConfigContext(config) { new ->
                    coroutineScope.launch {
                        config = new
                        configRepository.write(new)
                        if (new.serverUri.isNotBlank()) {
                            httpClientProvider.recreate(new.serverUri)
                        }
                    }
                }
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (config.customBg && config.bgPath.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(config.bgPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxHeight(),
                        )
                    }
                    AppNavHost()
                    SplashPage()
                }
            }
        }
    }
}
