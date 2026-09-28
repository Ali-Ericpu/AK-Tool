package com.rainccup.aktool.core.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

typealias NestingMap<T> = Map<String, Map<String, T>>
typealias ListMap<T> = List<Map<String, T>>

inline fun <reified T> Map<String, *>.getTyped(key: String): T? = this[key] as? T

fun jsonToMap(element: JsonElement): Any? = when (element) {
    is JsonObject -> element.mapValues { jsonToMap(it.value) }
    is JsonArray -> element.map { jsonToMap(it) }
    is JsonPrimitive -> when {
        element.contentOrNull == null -> null
        element.isString -> element.content
        element.content.equals("true", ignoreCase = true) -> true
        element.content.equals("false", ignoreCase = true) -> false
        element.content.toIntOrNull() != null -> element.content.toInt()
        element.content.toLongOrNull() != null -> element.content.toLong()
        element.content.toDoubleOrNull() != null -> element.content.toDouble()
        else -> element.content
    }
}

inline fun <reified T : Map<String, *>> decodeToMap(text: String): T {
    val element = JsonUtil.json.parseToJsonElement(text)
    return jsonToMap(element) as? T ?: error("Decode failed")
}
