package com.rainccup.aktool.core.domain.usecase.config

import com.rainccup.aktool.core.data.datasource.ConfigRepository
import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.network.HttpClientProvider

/** 设置页：配置读写与游戏资源表更新。 */
class GetConfigUseCase(private val configRepository: ConfigRepository) {
    operator fun invoke(): AppConfig = configRepository.read()
}

class SaveConfigUseCase(
    private val configRepository: ConfigRepository,
    private val httpClientProvider: HttpClientProvider,
) {
    operator fun invoke(serverUri: String, uid: String, adminKey: String) {
        val cfg = configRepository.read()
        configRepository.write(cfg.copy(serverUri = serverUri, uid = uid, adminKey = adminKey))
        httpClientProvider.recreate(serverUri.ifBlank { cfg.serverUri })
    }
}

/**
 * 更新游戏资源表。
 * `serverUri` 为空时直接返回 false 且不发起请求（原先是设置页里的前置判断，
 * 现在收进 use case，页面只负责调用与提示）。
 */
class UpdateGameTableUseCase(private val gameTable: GameTableRepository) {
    suspend operator fun invoke(serverUri: String): Boolean {
        if (serverUri.isEmpty()) return false
        gameTable.refresh(serverUri)
        return true
    }
}
