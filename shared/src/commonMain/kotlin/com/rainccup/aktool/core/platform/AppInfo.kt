package com.rainccup.aktool.core.platform

/**
 * 运行时的构建信息（版本号、是否 debug）。
 *
 * 版本号直接读 Gradle 生成的 [BuildInfo]（`shared/build/generated/buildInfo`），
 * 所以宿主不需要再实现、也不需要注册任何构建信息端口：Android 不再依赖
 * AGP 的 `BuildConfig`，桌面也不再靠 `processResources` 展开 `version.properties`。
 */
class AppInfo {
    val versionName: String = BuildInfo.VERSION_NAME
    val versionCode: String = BuildInfo.VERSION_CODE.toString()
    val isDebug: Boolean = isDebugBuild
}
