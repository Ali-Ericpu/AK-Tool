package com.rainccup.aktool.core.repository

import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.NetworkConfig
import com.rainccup.aktool.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminRepositoryTest {
    @Test
    fun syncStatus_appliesCredentialsFromConfig() = runTest {
        var seenUid = ""
        val engine = MockEngine { request ->
            seenUid = request.headers["uid"] ?: ""
            respond("""{"msg":"ok","status":0,"type":"OK","data":null}""", HttpStatusCode.OK)
        }
        val net = NetworkConfig(baseUrl = "http://t")
        val api = ApiClient(net) { createHttpClient(net, engine) }
        val fakeConfig = object : ConfigSource {
            override fun current(): AppConfig = AppConfig(uid = "U1", adminKey = "A1")
        }
        val repo = AdminRepository(api, fakeConfig, net)
        runCatching { repo.syncStatus() }
        assertEquals("U1", seenUid)
    }
}
