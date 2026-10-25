package com.rainccup.aktool

import com.rainccup.aktool.core.common.AppBuildConfig
import java.util.Properties

/**
 * 桌面端的构建信息。
 *
 * Android 有 `BuildConfig`，桌面没有等价物，所以由 Gradle 在 `processResources` 时
 * 把版本号展开进 `version.properties`（见 desktopApp/build.gradle.kts），运行时再读回来。
 * 读不到就退回 `0.0.0`：更新检查会解析失败并安静跳过，不影响启动。
 */
class DesktopAppBuildConfig : AppBuildConfig {
    override val versionName: String = readVersion()
    override val versionCode: String = versionName
    override val isDebug: Boolean = System.getProperty("ak.tool.debug") == "true"

    private companion object {
        fun readVersion(): String = runCatching {
            DesktopAppBuildConfig::class.java
                .getResourceAsStream("/version.properties")
                ?.use { Properties().apply { load(it) }.getProperty("version") }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "0.0.0"
    }
}
