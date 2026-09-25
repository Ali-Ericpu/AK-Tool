package com.rainccup.aktool.core.data.datasource

import com.rainccup.aktool.core.common.JsonUtil
import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.platform.AppPaths
import com.rainccup.aktool.core.data.repository.ConfigSource

class ConfigRepository(private val paths: AppPaths) : ConfigSource {
    private val filePath: String
        get() = "${paths.configDir}/config.json"

    fun read(): AppConfig {
        val text = readFileText(filePath) ?: return AppConfig().also { write(it) }
        return JsonUtil.decode(text)
    }

    fun write(config: AppConfig) {
        writeFileText(filePath, JsonUtil.encodePretty(config))
    }

    override fun current(): AppConfig = read()
}
