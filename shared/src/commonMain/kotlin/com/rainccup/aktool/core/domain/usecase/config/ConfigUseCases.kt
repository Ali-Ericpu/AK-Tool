package com.rainccup.aktool.core.domain.usecase.config

import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.ConfigStore
import com.rainccup.aktool.core.model.AppConfig

/** 设置页：配置读写与游戏资源表更新。 */
class GetConfigUseCase(private val configStore: ConfigStore) {
    operator fun invoke(): AppConfig = configStore.current()
}

class SaveConfigUseCase(private val configStore: ConfigStore) {
    /** 写入配置并返回最终生效的 serverUri（空串时沿用旧值）。 */
    operator fun invoke(serverUri: String, uid: String, adminKey: String): String =
        configStore.save(serverUri, uid, adminKey)
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
