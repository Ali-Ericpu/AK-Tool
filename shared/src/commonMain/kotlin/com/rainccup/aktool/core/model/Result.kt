package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResult<T>(
    val msg: String,
    val status: Int,
    val type: String,
    val data: T? = null,
)
