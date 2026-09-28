package com.rainccup.aktool.core.domain.usecase.config

import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.ConfigStore

/** 设置页（SettingViewModel）的用例入口：配置读取与游戏资源表更新。 */
class SettingUseCase(
    private val configStore: ConfigStore,
    private val gameTable: GameTableRepository,
) {
    fun config() = configStore.current()

    /** serverUri 未配置时视为配置错误，页面据此提示并不发起请求。 */
    fun showConfigError(): Boolean = config().serverUri.isBlank()

    /** 更新游戏资源表；`serverUri` 为空时直接返回 false 且不发起请求。 */
    suspend fun updateGameTable(serverUri: String): Boolean {
        if (serverUri.isEmpty()) return false
        gameTable.refresh(serverUri)
        return true
    }
}
