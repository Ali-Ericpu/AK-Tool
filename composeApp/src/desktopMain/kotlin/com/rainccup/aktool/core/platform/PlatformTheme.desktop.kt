package com.rainccup.aktool.core.platform

class DesktopPlatformTheme : PlatformTheme {
    override fun supportsDynamicColor(): Boolean = false
}

actual fun createPlatformTheme(): PlatformTheme = DesktopPlatformTheme()
