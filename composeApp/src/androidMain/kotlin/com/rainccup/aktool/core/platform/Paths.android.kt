package com.rainccup.aktool.core.platform

import android.content.Context
import java.io.File

object AndroidContextHolder {
    lateinit var appContext: Context
}

class AndroidAppPaths(private val context: Context) : AppPaths {
    override val configDir: String
        get() = File(context.filesDir, "config").absolutePath
    override val dataDir: String
        get() = File(context.filesDir, "data").absolutePath

    override fun resolve(relativePath: String): String =
        File(context.filesDir, relativePath).absolutePath
}

actual fun createAppPaths(): AppPaths = AndroidAppPaths(AndroidContextHolder.appContext)
