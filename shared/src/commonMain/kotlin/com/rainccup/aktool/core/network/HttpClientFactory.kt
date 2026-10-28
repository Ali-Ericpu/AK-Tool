package com.rainccup.aktool.core.network

import com.rainccup.aktool.core.common.JsonUtil
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

fun createHttpClient(
    config: NetworkConfig,
    engine: HttpClientEngine? = null,
    expectSuccess: Boolean = true,
    isDebug: Boolean = false,
): HttpClient {
    val builder: HttpClientConfig<*>.() -> Unit = {
        this.expectSuccess = expectSuccess
        install(ContentNegotiation) {
            json(JsonUtil.json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.i(tag = "KTOR") { message }
                }
            }
            level = if (isDebug) LogLevel.ALL else LogLevel.INFO
        }
        defaultRequest {
            url(config.baseUrl.ifBlank { NetworkConfig.DEFAULT_BASE_URL })
            contentType(ContentType.Application.Json)
        }
    }
    return if (engine != null) HttpClient(engine, builder) else HttpClient(builder)
}

class HttpClientProvider(
    private val config: NetworkConfig,
    private val isDebug: Boolean = false,
) {
    @Volatile
    private var cached: HttpClient? = null

    fun client(): HttpClient = cached ?: createHttpClient(config, isDebug = isDebug).also { cached = it }

    fun recreate(baseUrl: String) {
        config.baseUrl = baseUrl.ifBlank { NetworkConfig.DEFAULT_BASE_URL }
        cached?.close()
        cached = null
    }
}
