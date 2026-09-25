package com.rainccup.aktool.core.data.repository

import com.rainccup.aktool.core.data.datasource.ConfigRepository
import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.network.HttpClientProvider

/**
 * 配置的读写门面：写入后立刻把最终生效的 serverUri 应用到 HTTP 客户端。
 * 把「持久化 + 客户端重置」这对副作用放在 data 层，domain 层就只需依赖 core.data，
 * 不必直接引用 core.network。
 */
class ConfigStore(
    private val configRepository: ConfigRepository,
    private val httpClientProvider: HttpClientProvider,
) {
    fun current(): AppConfig = configRepository.read()

    /** 写入配置；serverUri 为空时沿用旧值，并返回最终生效的地址。 */
    fun save(serverUri: String, uid: String, adminKey: String): String {
        val cfg = configRepository.read()
        val effective = serverUri.ifBlank { cfg.serverUri }
        configRepository.write(cfg.copy(serverUri = serverUri, uid = uid, adminKey = adminKey))
        httpClientProvider.recreate(effective)
        return effective
    }
}
