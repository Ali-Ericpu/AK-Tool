package com.rainccup.aktool.core.platform

import org.koin.dsl.module

val platformModule = module {
    single<AppPaths> { createAppPaths() }
    single<ClipboardPort> { createClipboard() }
    single<FilePicker> { createFilePicker() }
    single<PlatformTheme> { createPlatformTheme() }
}
