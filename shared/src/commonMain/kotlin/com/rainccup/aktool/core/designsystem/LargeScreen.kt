package com.rainccup.aktool.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * 大屏断点：窗口宽度达到该值就切大屏布局（左侧导航栏、页面内每排两个组件等）。
 * 600dp 是 Material 3 里 compact → medium 的分界。
 */
val LargeScreenBreakpoint = 600.dp

/**
 * 当前是否处于大屏模式，由 `AppNavHost` 依据窗口宽度提供。
 *
 * 做成 CompositionLocal 是为了让"大屏"只有一个判定处：导航栏是竖排还是底部、页面里怎么分栏，
 * 都跟着同一个值走，不会出现"导航栏已经竖排、页面却还按手机排"这种错位。
 *
 * 用 static 版本：它只在窗口跨越断点时变化，而那一刻整棵子树本来就要重新布局，
 * 不需要 `compositionLocalOf` 那种逐读取点失效的粒度。
 */
val LocalLargeScreen = staticCompositionLocalOf { false }
