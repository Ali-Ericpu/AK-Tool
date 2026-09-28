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

/**
 * 更多页（ExtraViewModel）的用例入口：10 个管理动作。
 *
 * 请求构造与结果校验放在这里；加载态、成功/失败提示（Messenger）仍由 ViewModel 负责，
 * use case 不碰 UI 与平台端口。
 */
class ExtraUseCase(
    private val admin: AdminRepository,
    private val gameTable: GameTableRepository,
) {
    /** 解锁全部干员前需要 excel 表就位，缺表返回 false，由页面提示。 */
    suspend fun init(): Boolean = gameTable.init()

    /** 好感度以「百分比档位」提交，这里先换算成表里的真实值。 */
    suspend fun unlockAllCharacters(body: UnlockAllCharRequest): ApiResult<JsonElement?> =
        admin.unlockAllChar(body.copy(favorPoint = gameTable.getRealFavPoint(body.favorPoint)))

    suspend fun unlockAllStages(): ApiResult<JsonElement?> = admin.unlockAllStages()

    suspend fun unlockAllFlags(): ApiResult<JsonElement?> = admin.unlockAllFlags()

    suspend fun addFlushMessage(body: AddFlushMessageRequest): ApiResult<JsonElement?> =
        admin.addFlushMessage(body)

    suspend fun gainItem(body: Item): ApiResult<JsonElement?> =
        admin.gainItem(GainItemRequest(listOf(body)))

    suspend fun resetActivity(body: ResetActivityRequest): ApiResult<JsonElement?> =
        admin.resetActivity(body)

    suspend fun registerAccount(body: RegisterAccountRequest): ApiResult<JsonElement?> =
        admin.registerAccount(body)

    suspend fun resetIntegratedStrategies(): ApiResult<JsonElement?> = admin.resetRlv2()

    /** 验证码表；数据缺失时抛异常（与原实现一致）。 */
    suspend fun syncValidCode(): Map<String, String> {
        val result = admin.syncValidCode()
        return result.data ?: throw RuntimeException(result.msg)
    }

    /** 查询账号并返回账号名；复制到剪贴板与提示由 ViewModel 负责。 */
    suspend fun queryAccount(): String = admin.queryAccountByUID().data!!["account"]!!
}
