package com.rainccup.aktool.core.domain.usecase.admin

import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.model.AddFlushMessageRequest
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.GainItemRequest
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.RegisterAccountRequest
import com.rainccup.aktool.core.model.ResetActivityRequest
import com.rainccup.aktool.core.model.UnlockAllCharRequest
import kotlinx.serialization.json.JsonElement

/*
 * 额外页的 10 个管理请求。请求构造与结果校验放在这里；
 * 加载态、成功/失败提示（Messenger）仍由 ViewModel 负责，use case 不碰 UI。
 */

class UnlockAllCharactersUseCase(
    private val admin: AdminRepository,
    private val gameTableRepository: GameTableRepository
) {
    suspend fun init(): Boolean = gameTableRepository.init()
    suspend operator fun invoke(body: UnlockAllCharRequest): ApiResult<JsonElement?> =
        admin.unlockAllChar(body.copy(favorPoint = gameTableRepository.getRealFavPoint(body.favorPoint)))
}

class UnlockAllStagesUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(): ApiResult<JsonElement?> = admin.unlockAllStages()
}

class UnlockAllFlagsUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(): ApiResult<JsonElement?> = admin.unlockAllFlags()
}

class AddFlushMessageUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(body: AddFlushMessageRequest): ApiResult<JsonElement?> =
        admin.addFlushMessage(body)
}

class GainItemUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(body: Item): ApiResult<JsonElement?> =
        admin.gainItem(GainItemRequest(listOf(body)))
}

class ResetActivityUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(body: ResetActivityRequest): ApiResult<JsonElement?> =
        admin.resetActivity(body)
}

class RegisterAccountUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(body: RegisterAccountRequest): ApiResult<JsonElement?> =
        admin.registerAccount(body)
}

class ResetIntegratedStrategiesUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(): ApiResult<JsonElement?> = admin.resetRlv2()
}

/** 验证码表；数据缺失时抛异常（与原实现一致）。 */
class SyncValidCodeUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(): Map<String, String> {
        val result = admin.syncValidCode()
        return result.data ?: throw RuntimeException(result.msg)
    }
}

/** 查询账号并返回账号名；复制到剪贴板与提示由 ViewModel 负责。 */
class QueryAccountByUidUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(): String = admin.queryAccountByUID().data!!["account"]!!
}
