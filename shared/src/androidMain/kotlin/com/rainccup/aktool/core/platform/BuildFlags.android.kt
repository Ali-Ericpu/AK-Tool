package com.rainccup.aktool.core.platform

import android.content.pm.ApplicationInfo

/**
 * Android 侧的可调试判定：直接读本应用 `AndroidManifest` 里的 `android:debuggable`。
 *
 * 等价于旧方案的 `BuildConfig.DEBUG`，但不需要 AGP 生成 `BuildConfig`。
 * [AndroidContextHolder.appContext] 在 `MainActivity.onCreate` 里第一行就赋值，
 * 而这行代码只在应用启动之后才会被读到（网络层/设置页），不存在取不到上下文的情况。
 */
actual val isDebugBuild: Boolean
    get() = AndroidContextHolder.appContext.applicationInfo.flags and
        ApplicationInfo.FLAG_DEBUGGABLE != 0
