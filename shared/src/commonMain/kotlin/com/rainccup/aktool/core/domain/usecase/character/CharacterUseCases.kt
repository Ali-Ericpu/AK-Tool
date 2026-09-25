package com.rainccup.aktool.core.domain.usecase.character

import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.GainItemRequest
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.SaveCharRequest
import kotlinx.serialization.json.JsonElement
import kotlin.math.min

/** 拉取并解析角色表。返回解析结果，异常交给调用方（UI 决定提示与收起加载动画）。 */
class LoadCharactersUseCase(
    private val admin: AdminRepository,
    private val gameTable: GameTableRepository,
) {
    suspend operator fun invoke(): LoadedCharacters {
        val nameToId = mutableMapOf<String, String>()
        gameTable.characterTable.forEach { (charId, charData) ->
            if (charId.startsWith("char_") && charData["displayNumber"] != null) {
                nameToId[charData["name"] as String] = charId
            }
        }
        val result = admin.syncCharacter()
        val payload = result.data ?: throw RuntimeException("数据异常")
        val characters = mutableMapOf<String, Character>()
        payload.forEach { (instId, char) ->
            runCatching { gameTable.getCharacterData(char.charId) }.onSuccess {
                characters[instId] = char.copy(
                    name = it["name"] as String,
                    profession = it["profession"] as String,
                    rarity = (it["rarity"] as String).substringAfter("_").toInt(),
                )
            }.onFailure { Logger.d { "Character_Init_CharData ${it.message}" } }
        }
        return LoadedCharacters(characters, nameToId)
    }

    data class LoadedCharacters(
        val characters: Map<String, Character>,
        val nameToId: Map<String, String>,
    )
}

/**
 * 保存干员：先按业务规则补全字段（原写在详情页保存按钮的 onClick 里），再提交。
 * `applyRules` 是纯函数，供 UI 先落地本地状态；`invoke` 负责提交。
 */
class SaveCharacterUseCase(
    private val admin: AdminRepository,
    private val gameTable: GameTableRepository,
) {
    /** 精英满级：解锁全部模组、未选中模组时选第一个、皮肤 #1 且有 #2 时切换到 #2。 */
    fun applyRules(char: Character): Character {
        var result = char
        if (result.evolvePhase == 2) {
            if (result.equip.isNotEmpty()) {
                val first = result.equip.keys.first()
                if (result.currentEquip == null) {
                    result = result.copy(currentEquip = first)
                }
                result.equip.values.forEach { if (it.locked == 1) it.unlock() }
            }
            if (result.skin == result.charId + "#1" &&
                gameTable.characterTable[result.charId]?.get("displayNumber") != null
            ) {
                result = result.copy(skin = result.charId + "#2")
            }
        } else if (result.evolvePhase < 2) {
            result = result.copy(
                currentTmpl = null,
                tmpl = null,
                currentEquip = null,
                equip = result.equip.mapValues { it.value.copy() },
                skills = result.skills.map { it.copy(specializeLevel = 0) },
            )
        }
        return if (result.level == gameTable.getMaxCharLevel(result.charId, result.evolvePhase)) {
            result.copy(exp = 0)
        } else {
            result
        }
    }

    /** 提交：好感度先换算成百分比点，再发请求。 */
    suspend operator fun invoke(char: Character): ApiResult<JsonElement?> =
        admin.saveCharacter(
            SaveCharRequest(
                char.instId,
                char.copy(favorPoint = gameTable.getFavPointPercent(char.favorPoint)),
            ),
        )
}

/** 新增干员（原 CharacterViewModel.gainChar）。 */
class GainCharacterUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(charId: String) {
        val result = admin.gainItem(GainItemRequest(listOf(Item(charId, "CHAR", 1))))
        if (result.status != 0) throw RuntimeException(result.msg)
    }
}

/** 精英等级变更时的派生字段重置（原 CharacterDetailViewModel.changeEvoPhase）。 */
class ChangeEvolvePhaseUseCase(private val gameTable: GameTableRepository) {
    operator fun invoke(char: Character, phase: Int): EvolvePhaseChange {
        var skillIndex = -1
        val skills = char.skills.mapIndexed { index, skill ->
            val unlock = if (phase >= index) {
                skillIndex = index
                1
            } else {
                0
            }
            skill.copy(unlock = unlock)
        }.toList()
        var currentEquip = char.currentEquip
        val equip = char.equip.mapValues { (equipId, data) ->
            if (phase >= 2) {
                currentEquip = equipId
                data.unlock()
            } else {
                currentEquip = null
                data.lock()
            }
        }
        val maxLevel = gameTable.getMaxCharLevel(char.charId, phase)
        val maxSkillLevel = if (phase < 1) 4 else 7
        return EvolvePhaseChange(
            character = char.copy(
                evolvePhase = phase,
                mainSkillLvl = min(maxSkillLevel, char.mainSkillLvl),
                skills = skills,
                defaultSkillIndex = skillIndex,
                currentEquip = currentEquip,
                equip = equip,
                level = min(maxLevel, char.level),
            ),
            maxLevel = maxLevel,
            maxSkillLevel = maxSkillLevel,
        )
    }

    data class EvolvePhaseChange(
        val character: Character,
        val maxLevel: Int,
        val maxSkillLevel: Int,
    )
}

/** 一次取回详情页需要的三项上限（原 CharacterDetailViewModel 的三个 updateMax*）。 */
class GetCharacterLimitsUseCase(private val gameTable: GameTableRepository) {
    operator fun invoke(charId: String, phase: Int): CharacterLimits = CharacterLimits(
        maxEvoPhase = gameTable.getMaxCharEvoLevel(charId),
        maxLevel = gameTable.getMaxCharLevel(charId, phase),
        maxSkillLevel = if (phase < 1) 4 else 7,
    )

    data class CharacterLimits(
        val maxEvoPhase: Int,
        val maxLevel: Int,
        val maxSkillLevel: Int,
    )
}
