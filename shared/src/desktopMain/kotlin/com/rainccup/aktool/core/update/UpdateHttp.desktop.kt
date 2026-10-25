package com.rainccup.aktool.core.update

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createUpdateHttpClient(): HttpClient = HttpClient(CIO) {
    configureUpdateClient()
    engine {
        maxConnectionsCount = 4
    }
}
