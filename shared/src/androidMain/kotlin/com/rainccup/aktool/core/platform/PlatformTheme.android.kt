package com.rainccup.aktool.core.platform

import android.os.Build

class AndroidPlatformTheme : PlatformTheme {
    override fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

actual fun createPlatformTheme(): PlatformTheme = AndroidPlatformTheme()
