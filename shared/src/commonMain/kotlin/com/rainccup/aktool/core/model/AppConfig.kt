package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val serverUri: String = "",
    val darkMode: Boolean = false,
    val dynamicColor: Boolean = true,
    /** 悬浮底栏是否使用液态玻璃（模糊 + 折射 + 高光）。关掉只看得到一个不透明底栏。 */
    val liquidGlass: Boolean = true,
    val customBg: Boolean = false,
    val uid: String = "",
    val adminKey: String = "",
    val bgPath: String = "",
    val primaryColor: ULong = 0x0UL,
)
