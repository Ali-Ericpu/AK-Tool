package org.doctorate.aktool.utils

/**
 * ClassName: MapUtil
 * Package: com.doctorate.ui.util
 * Description:
 * @author Raincc
 * @Create 2024/11/7 15:39
 * @Version 1.0
 */

typealias NestingMap = Map<String, Map<String, Any>>

typealias ListMap = List<Map<String, Any>>

inline fun <reified T> Map<String, Any>.get(key: String) = get(key) as? T