package com.rainccup.aktool.core.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

typealias NestingMap = Map<String, Map<String, Any?>>
typealias ListMap = List<Map<String, Any?>>

inline fun <reified T> Map<String, Any?>.getTyped(key: String): T? = this[key] as? T

fun jsonToMap(element: JsonElement): Any? = when (element) {
    is JsonObject -> element.mapValues { jsonToMap(it.value) }
    is JsonArray -> element.map { jsonToMap(it) }
    is JsonPrimitive -> when {
        element.isString -> element.content
        element.content.equals("true", ignoreCase = true) -> true
        element.content.equals("false", ignoreCase = true) -> false
        element.content.toIntOrNull() != null -> element.content.toInt()
        element.content.toLongOrNull() != null -> element.content.toLong()
        element.content.toDoubleOrNull() != null -> element.content.toDouble()
        else -> element.content
    }
}

fun decodeToMap(text: String): Map<String, Any?> {
    val element = JsonUtil.json.parseToJsonElement(text) as JsonObject
    return element.mapValues { jsonToMap(it.value) }
}
