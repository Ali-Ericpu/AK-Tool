package com.rainccup.aktool.core.platform

/**
 * 当前进程跑的是不是可调试构建。
 *
 * 版本号能写进生成源（[BuildInfo]），但 debug 与否是**运行时属性**：同一个 APK 的
 * release 变体和 debug 变体版本号一样，没法用一份共享常量表达，所以留在平台层用
 * expect/actual 各取各的：
 * - Android：`ApplicationInfo.FLAG_DEBUGGABLE`（等价于旧方案的 `BuildConfig.DEBUG`）；
 * - 桌面：`-Dak.tool.debug=true`（没有 debug 变体，默认 release）。
 */
expect val isDebugBuild: Boolean
