package com.rainccup.aktool.core.domain

import com.rainccup.aktool.core.data.datasource.GameTableRepository

/**
 * 游戏数据表（excel）的只读查询入口，属于域层。
 *
 * 页面只通过它读表，`feature` 因此不必依赖 `core.data`
 * （见 `ArchitectureRuleTest` 的目标依赖表：feature → domain 合法，feature → data 不合法）。
 * 写入/刷新资源仍走 `UpdateGameTableUseCase`。
 */
class GameTableQuery(private val repository: GameTableRepository) {

    val characterTable: Map<String, Map<String, Any?>>
        get() = repository.characterTable

//    fun init(): Boolean = repository.init()

    fun getCharacterData(charId: String): Map<String, Any?> = repository.getCharacterData(charId)

    fun getMaxCharEvoLevel(charId: String): Int = repository.getMaxCharEvoLevel(charId)

    fun getMaxCharLevel(charId: String, evoPhase: Int): Int =
        repository.getMaxCharLevel(charId, evoPhase)

    fun getRealFavPoint(percent: Int): Int = repository.getRealFavPoint(percent)

    fun getFavPointPercent(favPoint: Int): Int = repository.getFavPointPercent(favPoint)

    fun getEquipType(equipId: String): String = repository.getEquipType(equipId)
}
