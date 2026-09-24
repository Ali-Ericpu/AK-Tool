package com.rainccup.aktool

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AK Tool",
        state = rememberWindowState(width = 480.dp, height = 860.dp),
    ) {
        App()
    }
}
