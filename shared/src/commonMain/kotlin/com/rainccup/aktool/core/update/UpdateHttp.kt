package com.rainccup.aktool.core.update

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

/**
 * 更新专用的 HTTP 客户端，跟应用那个 [com.rainccup.aktool.core.network.HttpClientProvider]
 * 分开：那个实例带 `defaultRequest { url(baseUrl); contentType(json) }`，
 * 用绝对 URL 发 GET 时仍会带上 `Content-Type: application/json`；
 * 更重要的是它的引擎参数改不了，而下载链路需要单独约束（见各平台的 actual）。
 *
 * 这里**故意不装 ContentNegotiation**：它会往每个请求塞默认的 `Accept: application/json`，
 * 而 GitHub 的资产端点要靠 `Accept: application/octet-stream` 才会 302 到二进制——
 * 带上 application/json 就被当成"要元数据"，直接回 JSON（曾因此把 1.7KB 的 JSON 落成 .apk，
 * 安装时报 INSTALL_PARSE_FAILED_NOT_APK）。所以 JSON 由调用方手工解析。
 */
internal fun HttpClientConfig<*>.configureUpdateClient() {
    install(HttpTimeout) {
        connectTimeoutMillis = 20_000
        // 下载时两次读到数据之间的最长间隔，不是总时长
        socketTimeoutMillis = 60_000
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                co.touchlab.kermit.Logger.i(tag = "UPDATE_APK") { message }
            }
        }
        level = LogLevel.INFO
    }
}

expect fun createUpdateHttpClient(): HttpClient
