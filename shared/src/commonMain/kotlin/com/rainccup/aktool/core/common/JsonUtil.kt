package com.rainccup.aktool.core.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

object JsonUtil {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false

    }

    private val pretty: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    val prettyJson: Json get() = pretty

    fun <T> decode(text: String, serializer: KSerializer<T>): T =
        json.decodeFromString(serializer, text)

    inline fun <reified T> decode(text: String): T = json.decodeFromString(text)

    fun <T> encode(value: T, serializer: KSerializer<T>): String =
        json.encodeToString(serializer, value)

    inline fun <reified T> encode(value: T): String = json.encodeToString(value)

    fun <T> encodePretty(value: T, serializer: KSerializer<T>): String =
        pretty.encodeToString(serializer, value)

    inline fun <reified T> encodePretty(value: T): String = prettyJson.encodeToString(value)
}
