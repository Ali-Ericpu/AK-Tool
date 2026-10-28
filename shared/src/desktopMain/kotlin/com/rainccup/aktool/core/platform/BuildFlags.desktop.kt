package com.rainccup.aktool.core.platform

/**
 * 桌面端没有 debug 变体：默认按 release 处理，需要看协议日志时用
 * `-Dak.tool.debug=true`（`jpackage` 生成的启动器可在 `*.cfg` 里加 `-D` 参数）。
 */
actual val isDebugBuild: Boolean
    get() = System.getProperty("ak.tool.debug") == "true"
