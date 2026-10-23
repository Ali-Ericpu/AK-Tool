package com.rainccup.aktool.core.platform

import androidx.compose.runtime.Composable

/** 桌面端窗口没有系统状态栏，不需要同步。 */
@Composable
actual fun SyncSystemBarAppearance(darkTheme: Boolean) = Unit