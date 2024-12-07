package org.doctorate.aktool.network

import android.util.Log
import org.doctorate.aktool.config.AppConfig
import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Result
import org.doctorate.aktool.pojo.entity.Status
import org.doctorate.aktool.pojo.request.AddFlushMessageRequest
import org.doctorate.aktool.pojo.request.GainItemRequest
import org.doctorate.aktool.pojo.request.SaveCharRequest
import org.doctorate.aktool.pojo.request.SaveStatusRequest
import org.doctorate.aktool.pojo.request.UnlockAllCharRequest
import org.doctorate.aktool.utils.JsonUtil
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object Network {

    private const val TAG = "Create_Retrofit"
    private var service = Retrofit.Builder()
        .baseUrl("http://127.0.0.1")
        .addConverterFactory(JacksonConverterFactory.create(JsonUtil.mapper))
        .build()
        .create(NetworkService::class.java)

    init {
        initService()
    }

    fun initService(uri: String = AppConfig.config.serverUri): Boolean {
        try {
            service = Retrofit.Builder()
                .baseUrl(uri.also { Log.d(TAG, "URI: $it") })
                .addConverterFactory(JacksonConverterFactory.create(JsonUtil.mapper))
                .build()
                .create(NetworkService::class.java)
            return true
        } catch (_: Exception) {
            Log.e(TAG, "error uri : $uri")
            return false
        }
    }

    suspend fun syncCharacter(): Result<Map<String, Character>> {
        val config = AppConfig.config
        return service.syncCharacter(config.uid, config.adminKey)
    }

    suspend fun saveCharacter(body: SaveCharRequest): Result<String?> {
        val config = AppConfig.config
        return service.saveCharacter(config.uid, config.adminKey, body)
    }

    suspend fun gainItem(body: GainItemRequest): Result<Map<String, Any>?> {
        val config = AppConfig.config
        return service.gainItem(config.uid, config.adminKey, body)
    }

    suspend fun syncStatus(): Result<Status> {
        val config = AppConfig.config
        return service.syncStatus(config.uid, config.adminKey)
    }
    suspend fun saveStatus(saveStatusRequest: SaveStatusRequest): Result<Map<String, Any>?> {
        val config = AppConfig.config
        return service.saveStatus(config.uid, config.adminKey, saveStatusRequest)
    }

    suspend fun unlockAllChar(body: UnlockAllCharRequest): Result<Map<String, Any>?> {
        val config = AppConfig.config
        return service.unlockAllChar(config.uid, config.adminKey, body)
    }

    suspend fun unlockAllStages(): Result<Map<String, Any>?> {
        val config = AppConfig.config
        return service.unlockAllStages(config.uid, config.adminKey)
    }

    suspend fun unlockAllFlags(): Result<Map<String, Any>?> {
        val config = AppConfig.config
        return service.unlockAllFlags(config.uid, config.adminKey)
    }

    suspend fun addFlushMessage(body: AddFlushMessageRequest): Result<Map<String, Any>?> {
        return service.addFlushMessage(AppConfig.config.adminKey, body)
    }

}