package com.rainccup.aktool.core.platform

import androidx.compose.runtime.Composable

/**
 * 让系统状态栏 / 导航栏图标（时间、电量、手势条）的明暗跟随应用主题。
 *
 * Android 15（API 35）起对 targetSdk >= 35 的应用强制 edge-to-edge，界面内容会一直画到
 * 状态栏下面。这时图标颜色如果在主题里写死，深色主题就成了「深色图标压在深色背景上」，
 * 状态栏等于看不见；浅色主题反过来则是白图标压白底。所以在应用主题变化时同步一次。
 *
 * @param darkTheme 应用当前是否是深色主题，为 true 时状态栏用浅色（白色）图标。
 */
@Composable
expect fun SyncSystemBarAppearance(darkTheme: Boolean)