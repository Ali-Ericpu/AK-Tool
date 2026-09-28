package com.rainccup.aktool.core.domain.usecase.character

import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.GainItemRequest
import com.rainccup.aktool.core.model.Item

/**
 * 干员列表页（CharacterViewModel）的用例入口：读表、同步干员、补发干员。
 *
 * 列表解析的异常交给调用方（UI 决定提示与收起加载动画）。
 */
class CharacterUseCase(
    private val admin: AdminRepository,
    private val gameTable: GameTableRepository,
) {
    /** 确保三张 excel 表已加载；缺表返回 false，由页面提示「数据缺失」。 */
    suspend fun init(): Boolean = gameTable.init()

    /** 干员是否**存在**，与页面表单的 error 语义一致。 */
    fun existChar(charId: String): Boolean = gameTable.characterTable[charId] == null

    suspend fun loadCharacters(): LoadedCharacters {
        val table = gameTable.characterTable
        val nameToId = LinkedHashMap<String, String>(table.size)
        table.forEach { (charId, charData) ->
            if (charId.startsWith("char_") && charData["displayNumber"] != null) {
                nameToId[charData["name"] as String] = charId
            }
        }

        val result = admin.syncCharacter()
        // data 为 null 即接口层失败
        val payload = result.data ?: error("数据异常")

        val characters = LinkedHashMap<String, Character>(payload.size)
        payload.forEach { (instId, char) ->
            val charData = table[char.charId]
            // 表里没有该 charId：跳过
            if (charData == null) {
                Logger.d { "Character_Init_CharData ${char.charId} not in table" }
                return@forEach
            }
            characters[instId] = char.copy(
                name = charData["name"] as String,
                profession = charData["profession"] as String,
                rarity = (charData["rarity"] as String).substringAfter("_").toInt(),
            )
        }
        return LoadedCharacters(characters, nameToId)
    }

    /** 新增干员。 */
    suspend fun gainCharacter(charId: String) {
        val result = admin.gainItem(GainItemRequest(listOf(Item(charId, "CHAR", 1))))
        if (result.status != 0) throw RuntimeException(result.msg)
    }

    data class LoadedCharacters(
        val characters: Map<String, Character>,
        val nameToId: Map<String, String>,
    )
}
