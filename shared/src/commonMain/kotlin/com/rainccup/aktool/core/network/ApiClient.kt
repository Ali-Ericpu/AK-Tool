package com.rainccup.aktool.core.network

import com.rainccup.aktool.core.model.AddFlushMessageRequest
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.GainItemRequest
import com.rainccup.aktool.core.model.RegisterAccountRequest
import com.rainccup.aktool.core.model.ResetActivityRequest
import com.rainccup.aktool.core.model.SaveCharRequest
import com.rainccup.aktool.core.model.SaveStatusRequest
import com.rainccup.aktool.core.model.Status
import com.rainccup.aktool.core.model.UnlockAllCharRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.JsonElement

class ApiClient(
    private val config: NetworkConfig,
    private val clientProvider: () -> HttpClient,
) {
    suspend fun syncCharacter(): ApiResult<Map<String, Character>> =
        clientProvider()
            .get("admin/character/sync") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun saveCharacter(body: SaveCharRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/character/save") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun gainItem(body: GainItemRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/gainItem") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun syncStatus(): ApiResult<Status> =
        clientProvider()
            .get("admin/status/sync") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun saveStatus(body: SaveStatusRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/status/save") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun unlockAllChar(body: UnlockAllCharRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/unlockAllChar") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun unlockAllStages(): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/unlockAllStages") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun unlockAllFlags(): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/unlockAllFlags") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun addFlushMessage(body: AddFlushMessageRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/addFlushMessage") {
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun resetActivity(body: ResetActivityRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/resetActivity") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun registerAccount(body: RegisterAccountRequest): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/account/register") {
                header("adminKey", config.adminKey)
                setBody(body)
            }
            .body()

    suspend fun resetAccount(): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/account/reset") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun syncValidCode(): ApiResult<Map<String, String>> =
        clientProvider()
            .get("admin/syncValidCode") {
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun resetRlv2(): ApiResult<JsonElement?> =
        clientProvider()
            .post("admin/rlv2/reset") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun queryAccountByUID(): ApiResult<Map<String, String>> =
        clientProvider()
            .post("admin/account/queryAccountByUID") {
                header("uid", config.uid)
                header("adminKey", config.adminKey)
            }
            .body()

    suspend fun downloadText(url: String): String = clientProvider().get(url).bodyAsText()
}
