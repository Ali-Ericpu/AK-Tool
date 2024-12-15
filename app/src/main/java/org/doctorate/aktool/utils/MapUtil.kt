package org.doctorate.aktool.utils

typealias NestingMap = Map<String, Map<String, Any>>

typealias ListMap = List<Map<String, Any>>

inline fun <reified T> Map<String, Any>.get(key: String) = get(key) as? T