package com.rainccup.aktool.core.platform

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

class AndroidPlatformTheme : PlatformTheme {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    override fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

actual fun createPlatformTheme(): PlatformTheme = AndroidPlatformTheme()
