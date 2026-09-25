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
        mainSkillLvl: Float,
        specializeLevel: Float,
        equipLevel: Float,
    ): EvolvePhaseEffects {
        val skill = if (phase < 1) min(4f, mainSkillLvl) else mainSkillLvl
        val spec = if (phase < 2) 0f else specializeLevel
        val equip = if (phase < 1) 1f else equipLevel
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
    fun normalizeSpecializeLevel(phase: Float, mainSkillLvl: Float, value: Float): Float =
        if (phase < 2f || mainSkillLvl < 7f) 0f else value

    /** 模组等级：精英<2 时必须为 1。 */
    fun normalizeEquipLevel(phase: Float, value: Float): Float =
        if (phase < 2f) 1f else value

    fun maxLevelFor(phase: Int): Float = when (phase) {
        0 -> 50f
        1 -> 80f
        else -> 90f
    }

    data class EvolvePhaseEffects(
        val mainSkillLvl: Float,
        val specializeLevel: Float,
        val equipLevel: Float,
        val maxLevel: Float,
    )
}
