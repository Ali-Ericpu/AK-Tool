package org.doctorate.aktool.pojo.entity

data class Result<T>(
    val msg: String,
    val status: Int,
    val type: String,
    val data: T?
)