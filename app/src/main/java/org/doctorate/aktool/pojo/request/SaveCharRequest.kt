package org.doctorate.aktool.pojo.request

import org.doctorate.aktool.pojo.entity.Character

data class SaveCharRequest(
    val charInstId: Int,
    val char: Character
)