package com.rainccup.aktool.core.domain.usecase.character

import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.SaveCharRequest
import kotlinx.serialization.json.JsonElement
import kotlin.math.min

/**
 * 干员详情页（CharacterDetailViewModel）的用例入口：上限查询、精英等级联动、保存。
 *
 * 保存拆成**纯函数 + I/O**：`applySaveRules` 供 UI 先把结果落进本地状态再提交，
 * 避免规则被应用两次。
 */
class CharacterDetailUseCase(
    private val admin: AdminRepository,
    private val gameTable: GameTableRepository,
) {
    /** 一次取回详情页需要的三项上限（原 CharacterDetailViewModel 的三个 updateMax*）。 */
    fun limits(charId: String, phase: Int): CharacterLimits = CharacterLimits(
        maxEvoPhase = gameTable.getMaxCharEvoLevel(charId),
        maxLevel = gameTable.getMaxCharLevel(charId, phase),
        maxSkillLevel = if (phase < 1) 4 else 7,
    )

    fun favPointPercent(favPoint: Int): Int = gameTable.getFavPointPercent(favPoint)

    /** 精英等级变更时的派生字段重置（原 CharacterDetailViewModel.changeEvoPhase）。 */
    fun changeEvolvePhase(char: Character, phase: Int): EvolvePhaseChange {
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

    /** 精英满级：解锁全部模组、未选中模组时选第一个、皮肤 #1 且有 #2 时切换到 #2。 */
    fun applySaveRules(char: Character): Character {
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
    suspend fun saveCharacter(char: Character): ApiResult<JsonElement?> =
        admin.saveCharacter(
            SaveCharRequest(
                char.instId,
                char.copy(favorPoint = gameTable.getRealFavPoint(char.favorPoint)),
            ),
        )

    data class CharacterLimits(
        val maxEvoPhase: Int,
        val maxLevel: Int,
        val maxSkillLevel: Int,
    )

    data class EvolvePhaseChange(
        val character: Character,
        val maxLevel: Int,
        val maxSkillLevel: Int,
    )
}
