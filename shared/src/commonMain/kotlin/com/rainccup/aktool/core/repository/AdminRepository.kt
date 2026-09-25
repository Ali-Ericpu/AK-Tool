package com.rainccup.aktool.core.repository

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
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.NetworkConfig
import kotlinx.serialization.json.JsonElement

class AdminRepository(
    private val api: ApiClient,
    private val configSource: ConfigSource,
    private val networkConfig: NetworkConfig,
) {
    private fun syncCredentials() {
        val cfg = configSource.current()
        networkConfig.uid = cfg.uid
        networkConfig.adminKey = cfg.adminKey
    }

    suspend fun syncCharacter(): ApiResult<Map<String, Character>> {
        syncCredentials()
        return api.syncCharacter()
    }

    suspend fun saveCharacter(body: SaveCharRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.saveCharacter(body)
    }

    suspend fun gainItem(body: GainItemRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.gainItem(body)
    }

    suspend fun syncStatus(): ApiResult<Status> {
        syncCredentials()
        return api.syncStatus()
    }

    suspend fun saveStatus(body: SaveStatusRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.saveStatus(body)
    }

    suspend fun unlockAllChar(body: UnlockAllCharRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.unlockAllChar(body)
    }

    suspend fun unlockAllStages(): ApiResult<JsonElement?> {
        syncCredentials()
        return api.unlockAllStages()
    }

    suspend fun unlockAllFlags(): ApiResult<JsonElement?> {
        syncCredentials()
        return api.unlockAllFlags()
    }

    suspend fun addFlushMessage(body: AddFlushMessageRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.addFlushMessage(body)
    }

    suspend fun resetActivity(body: ResetActivityRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.resetActivity(body)
    }

    suspend fun registerAccount(body: RegisterAccountRequest): ApiResult<JsonElement?> {
        syncCredentials()
        return api.registerAccount(body)
    }

    suspend fun syncValidCode(): ApiResult<Map<String, String>> {
        syncCredentials()
        return api.syncValidCode()
    }

    suspend fun resetRlv2(): ApiResult<JsonElement?> {
        syncCredentials()
        return api.resetRlv2()
    }

    suspend fun queryAccountByUID(): ApiResult<Map<String, String>> {
        syncCredentials()
        return api.queryAccountByUID()
    }
}
