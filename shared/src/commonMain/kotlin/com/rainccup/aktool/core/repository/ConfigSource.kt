package com.rainccup.aktool.core.repository

import com.rainccup.aktool.core.model.AppConfig

interface ConfigSource {
    fun current(): AppConfig
}
