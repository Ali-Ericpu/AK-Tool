package com.rainccup.aktool.core.network

import com.rainccup.aktool.core.model.SaveStatusRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiClientTest {

    @Test
    fun syncStatus_sendsAuthHeaders() = runTest {
        var capturedUid = ""
        var capturedKey = ""
        var capturedPath = ""
        val engine = MockEngine { request ->
            capturedPath = request.url.encodedPath
            capturedUid = request.headers["uid"] ?: ""
            capturedKey = request.headers["adminKey"] ?: ""
            respond(
                content = """{"msg":"ok","status":0,"type":"OK","data":{
                    "nickName":"n","nickNumber":"1","level":1,"exp":0,"socialPoint":0,
                    "gachaTicket":0,"tenGachaTicket":0,"instantFinishTicket":0,"hggShard":0,
                    "lggShard":0,"recruitLicense":0,"progress":0,"buyApRemainTimes":0,
                    "apLimitUpFlag":0,"uid":"42","flags":{},"ap":1,"maxAp":2,
                    "androidDiamond":0,"iosDiamond":0,"diamondShard":0,"gold":0,
                    "practiceTicket":0,"lastRefreshTs":0,"lastApAddTime":0,
                    "registerTs":0,"lastOnlineTs":0,"serverName":"TERRA","avatarId":"",
                    "resume":"","friendNumLimit":0,"monthlySubscriptionStartTime":0,
                    "monthlySubscriptionEndTime":0,"secretary":"","secretarySkinId":"",
                    "tipMonthlyCardExpireTs":0,"classicShard":0,"classicGachaTicket":0,
                    "classicTenGachaTicket":0
                }}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val config = NetworkConfig(baseUrl = "http://test.local", uid = "42", adminKey = "secret")
        val api = ApiClient(config) { createHttpClient(config, engine) }
        val result = api.syncStatus()
        assertEquals(0, result.status)
        assertEquals("42", result.data?.uid)
        assertEquals("admin/status/sync", capturedPath.trimStart('/'))
        assertEquals("42", capturedUid)
        assertEquals("secret", capturedKey)
    }

    @Test
    fun saveStatus_omitsNullFields() = runTest {
        var capturedBody = ""
        val engine = MockEngine { request ->
            capturedBody = when (val body = request.body) {
                is io.ktor.http.content.TextContent -> body.text
                else -> body.toString()
            }
            respond(
                """{"msg":"ok","status":0,"type":"OK","data":null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val config = NetworkConfig(baseUrl = "http://test.local", uid = "1", adminKey = "k")
        val api = ApiClient(config) { createHttpClient(config, engine) }
        api.saveStatus(SaveStatusRequest(level = 10))
        assertEquals(true, "\"level\":10" in capturedBody)
        assertEquals(false, "nickName" in capturedBody)
    }
}
