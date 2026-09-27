package com.rainccup.aktool.core.domain.model

import kotlin.math.min

/**
 * 「解锁所有干员」弹窗的滑块联动规则（纯函数）。
 * 原先是 `ExtraPage` 三个 `onValueChangeFinished` 里的内联分支，属于业务规则。
 */
object UnlockAllCharRules {

    /** 精英等级变化后的下游字段：技能等级夹取、专精清零、模组等级、等级上限。 */
    fun onEvolvePhaseChanged(
        phase: Int,
        mainSkillLvl: Int,
        specializeLevel: Int,
        equipLevel: Int,
    ): EvolvePhaseEffects {
        val skill = if (phase < 1) min(4, mainSkillLvl) else mainSkillLvl
        val spec = if (phase < 2) 0 else specializeLevel
        val equip = if (phase < 1) 1 else equipLevel
        return EvolvePhaseEffects(
            mainSkillLvl = skill,
            specializeLevel = spec,
            equipLevel = equip,
            maxLevel = maxLevelFor(phase),
        )
    }

    /** 精英 0 时技能等级不允许超过 4。 */
    fun clampSkillLevel(phase: Int, level: Int): Int =
        if (phase == 0 && level > 4) 4 else level

    /** 专精等级：精英<2 或技能等级<7 时必须归零。 */
    fun normalizeSpecializeLevel(phase: Int, mainSkillLvl: Int, value: Int): Int =
        if (phase < 2 || mainSkillLvl < 7) 0 else value

    /** 模组等级：精英<2 时必须为 1。 */
    fun normalizeEquipLevel(phase: Int, value: Int): Int =
        if (phase < 2) 1 else value

    fun maxLevelFor(phase: Int): Int = when (phase) {
        0 -> 50
        1 -> 80
        else -> 90
    }

    data class EvolvePhaseEffects(
        val mainSkillLvl: Int,
        val specializeLevel: Int,
        val equipLevel: Int,
        val maxLevel: Int,
    )
}
