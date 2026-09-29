package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val serverUri: String = "",
    val darkMode: Boolean = false,
    val dynamicColor: Boolean = true,
    val customBg: Boolean = false,
    val uid: String = "",
    val adminKey: String = "",
    val bgPath: String = "",
    val primaryColor: ULong = 0x0UL,
)
