package com.rainccup.aktool

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import com.rainccup.aktool.core.datastore.ConfigRepository
import com.rainccup.aktool.ui.navigation.AppNavHost
import com.rainccup.aktool.ui.splash.SplashPage
import com.rainccup.aktool.ui.theme.AKToolTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val configRepository: ConfigRepository = koinInject()
    var config by remember { mutableStateOf(configRepository.read()) }

    val darkMode = config.darkMode || isSystemInDarkTheme()
    AKToolTheme(darkTheme = darkMode) {
        Surface(Modifier.fillMaxSize()) {
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
