package com.rainccup.aktool.core.common

/**
 * 构建信息端口：由宿主平台（androidApp / desktopApp）实现并注入。
 *
 * 放在 core.common 而不是 app 包——app 是聚合层，规则上不允许被 core/feature 反向依赖，
 * 但网络层、平台层、更新模块和设置页都要读版本号/调试开关。
 */
interface AppBuildConfig {
    val versionName: String
    val versionCode: String
    val isDebug: Boolean
}

class AppInfo(private val config: AppBuildConfig) {
    val versionName = config.versionName
    val versionCode = config.versionCode
    val isDebug = config.isDebug
}
