package org.doctorate.aktool.network

import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Result
import org.doctorate.aktool.pojo.entity.Status
import org.doctorate.aktool.pojo.request.AddFlushMessageRequest
import org.doctorate.aktool.pojo.request.GainItemRequest
import org.doctorate.aktool.pojo.request.RegisterAccountRequest
import org.doctorate.aktool.pojo.request.ResetActivityRequest
import org.doctorate.aktool.pojo.request.SaveCharRequest
import org.doctorate.aktool.pojo.request.SaveStatusRequest
import org.doctorate.aktool.pojo.request.UnlockAllCharRequest
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
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String,
        @Body body: SaveCharRequest
    ): Result<Map<String, Any>?>

    @POST("admin/gainItem")
    suspend fun gainItem(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String,
        @Body body: GainItemRequest
    ): Result<Map<String, Any>?>

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

    @POST("admin/unlockAllChar")
    suspend fun unlockAllChar(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String,
        @Body body: UnlockAllCharRequest
    ): Result<Map<String, Any>?>

    @POST("admin/unlockAllStages")
    suspend fun unlockAllStages(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String
    ): Result<Map<String, Any>?>

    @POST("admin/unlockAllFlags")
    suspend fun unlockAllFlags(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String
    ): Result<Map<String, Any>?>

    @POST("admin/addFlushMessage")
    suspend fun addFlushMessage(
        @Header("adminKey") adminKey: String,
        @Body body: AddFlushMessageRequest
    ): Result<Map<String, Any>?>

    @POST("admin/resetActivity")
    suspend fun resetActivity(
        @Header("uid") uid: String,
        @Header("adminKey") adminKey: String,
        @Body body: ResetActivityRequest
    ): Result<Map<String, Any>?>


    @POST("admin/registerAccount")
    suspend fun registerAccount(
        @Header("adminKey") adminKey: String,
        @Body body: RegisterAccountRequest
    ): Result<Map<String, Any>?>

    @GET("admin/syncValidCode")
    suspend fun syncValidCode(@Header("adminKey") adminKey: String): Result<Map<String, String>>

}