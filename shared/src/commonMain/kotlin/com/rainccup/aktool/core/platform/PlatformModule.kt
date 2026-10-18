package com.rainccup.aktool.core.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val platformModule = module {
    single<AppPaths> { createAppPaths() }
    single<ClipboardPort> { createClipboard() }
    single<FilePicker> { createFilePicker() }
    single<PlatformTheme> { createPlatformTheme() }

    // 活到进程结束的作用域：页面销毁后仍需跑完的操作（例如保存并返回）用它。
    single { AppScope(CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
}
