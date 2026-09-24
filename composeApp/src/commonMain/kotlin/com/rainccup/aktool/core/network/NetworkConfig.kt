package com.rainccup.aktool.core.network

class NetworkConfig(
    var baseUrl: String = DEFAULT_BASE_URL,
    var uid: String = "",
    var adminKey: String = "",
) {
    companion object {
        const val DEFAULT_BASE_URL: String = "http://127.0.0.1"
    }
}
