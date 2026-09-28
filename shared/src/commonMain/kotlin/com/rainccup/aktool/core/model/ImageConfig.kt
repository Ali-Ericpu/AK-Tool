package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64

@Serializable
data class ImageConfig(
    val portrait: String,
    val skill: String,
    val equip: String
) {
    companion object {
        val default by lazy {
            val portrait = "aHR0cHM6Ly93ZWIuaHljZG4uY24vYXJrbmlnaHRzL2dhbWUvYXNzZXRzL2NoYXJfc2tpbi9wb3J0cmFpdC8="
            val skill = "aHR0cHM6Ly93ZWIuaHljZG4uY24vYXJrbmlnaHRzL2dhbWUvYXNzZXRzL2NoYXJfc2tpbGwv"
            val equip = "aHR0cHM6Ly93ZWIuaHljZG4uY24vYXJrbmlnaHRzL2dhbWUvYXNzZXRzL3VuaWVxdWlwL3R5cGUv"
            ImageConfig(
                portrait = Base64.decode(portrait).decodeToString(),
                skill = Base64.decode(skill).decodeToString(),
                equip = Base64.decode(equip).decodeToString()
            )
        }
        operator fun invoke() = default
    }
}
