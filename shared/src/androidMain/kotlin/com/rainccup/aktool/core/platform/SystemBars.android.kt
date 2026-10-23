package com.rainccup.aktool.core.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

@Composable
actual fun SyncSystemBarAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController ?: return@SideEffect
            val lightMask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            // 深色主题清掉 LIGHT 位 -> 白色图标；浅色主题置位 -> 深色图标
            controller.setSystemBarsAppearance(if (darkTheme) 0 else lightMask, lightMask)
        } else {
            // API 28-29 没有 WindowInsetsController，只能改 systemUiVisibility 的 LIGHT 位
            val decor = window.decorView
            @Suppress("DEPRECATION")
            val lightFlags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            @Suppress("DEPRECATION")
            val current = decor.systemUiVisibility
            @Suppress("DEPRECATION")
            val updated = if (darkTheme) current and lightFlags.inv() else current or lightFlags
            if (updated != current) {
                @Suppress("DEPRECATION")
                decor.systemUiVisibility = updated
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}