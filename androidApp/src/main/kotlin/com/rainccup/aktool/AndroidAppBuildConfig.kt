package com.rainccup.aktool

import com.rainccup.aktool.core.common.AppBuildConfig

class AndroidAppBuildConfig: AppBuildConfig {
    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: String = BuildConfig.VERSION_CODE.toString()
    override val isDebug: Boolean = BuildConfig.DEBUG
}