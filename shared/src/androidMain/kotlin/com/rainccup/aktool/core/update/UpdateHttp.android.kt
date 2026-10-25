package com.rainccup.aktool.core.update

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Protocol

/**
 * Android 侧强制 HTTP/1.1。
 *
 * 国内常见的是 TUN 模式代理/VPN：它对 ALPN 协商出来的 h2 连接，回的是 HTTP/1.1 的字节流，
 * OKHttp 读到就不是 h2 前言，抛 `java.io.IOException: Required SETTINGS preface not received`。
 * GitHub 的下载链路（github.com → objects.githubusercontent.com）本身 h1/h2 都支持，
 * 固定走 h1 既没有功能损失，也绕开这类中间设备。
 */
actual fun createUpdateHttpClient(): HttpClient = HttpClient(OkHttp) {
    configureUpdateClient()
    engine {
        config {
            protocols(listOf(Protocol.HTTP_1_1))
            retryOnConnectionFailure(true)
            followRedirects(true)
            followSslRedirects(true)
        }
    }
}
