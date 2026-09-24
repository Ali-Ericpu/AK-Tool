package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String,
    val type: String,
    val count: Int,
)
