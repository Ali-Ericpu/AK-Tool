package com.rainccup.aktool.core.platform

actual fun urlEncode(value: String): String =
    java.net.URLEncoder.encode(value, "UTF-8")
