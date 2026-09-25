package com.rainccup.aktool.core.common

import androidx.compose.runtime.compositionLocalOf
import com.rainccup.aktool.core.model.AppConfig

typealias OnConfigChange = (AppConfig) -> Unit

class AppConfigContext(
    val config: AppConfig = AppConfig(),
    val onConfigChange: OnConfigChange = {},
)

val LocalAppConfig = compositionLocalOf { AppConfigContext { } }
