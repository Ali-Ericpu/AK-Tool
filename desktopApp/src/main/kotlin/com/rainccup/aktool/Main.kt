package com.rainccup.aktool

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rainccup.aktool.app.App
import com.rainccup.aktool.app.appModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(appModule) }
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AK Tool",
            state = rememberWindowState(width = 480.dp, height = 860.dp),
        ) {
            App()
        }
    }
}
