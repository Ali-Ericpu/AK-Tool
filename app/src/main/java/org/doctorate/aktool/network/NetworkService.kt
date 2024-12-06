package org.doctorate.aktool.network

import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Result
import org.doctorate.aktool.pojo.entity.Status
import org.doctorate.aktool.pojo.request.GainItemRequest
import org.doctorate.aktool.pojo.request.SaveCharRequest
import org.doctorate.aktool.pojo.request.SaveStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface NetworkService {

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

    @GET("admin/status/sync")
    suspend fun syncStatus(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String
    ): Result<Status>

    @POST("admin/status/save")
    suspend fun saveStatus(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String,
        @Body body: SaveStatusRequest
    ): Result<Map<String, Any>?>

}