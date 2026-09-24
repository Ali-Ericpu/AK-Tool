package com.rainccup.aktool.core.platform

import java.io.File

class DesktopAppPaths : AppPaths {
    private val root = File(System.getProperty("user.home"), ".aktool")

    override val configDir: String
        get() = File(root, "config").absolutePath
    override val dataDir: String
        get() = File(root, "data").absolutePath

    override fun resolve(relativePath: String): String = File(root, relativePath).absolutePath
}

actual fun createAppPaths(): AppPaths = DesktopAppPaths()
