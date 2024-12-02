package org.doctorate.aktool.network.retrofit

import org.doctorate.aktool.network.Network
import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Result
import org.doctorate.aktool.pojo.request.GainItemRequest
import org.doctorate.aktool.pojo.request.SaveCharRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CharacterService {

    @GET("admin/character/sync")
    suspend fun syncCharacter(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String
    ): Result<Map<String, Character>>

    @POST("admin/character/save")
    suspend fun saveCharacter(
        @Header("adminKey") adminKey: String,
        @Header("uid") uid: String,
        @Body body: SaveCharRequest
    ): Result<String?>

    @POST("admin/gainItem")
    suspend fun gainItem(
        @Header("adminKey") adminKey: String,
        @Header("uid") uid: String,
        @Body body: GainItemRequest
    ): Result<String?>

    companion object {
        fun instance() = Network.createService(CharacterService::class.java)
    }
}