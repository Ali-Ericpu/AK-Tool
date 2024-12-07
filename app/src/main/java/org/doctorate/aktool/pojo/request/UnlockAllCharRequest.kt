package org.doctorate.aktool.pojo.request

data class UnlockAllCharRequest(
    val favorPoint: Int,
    val potentialRank: Int,
    val specializeLevel: Int,
    val mainSkillLvl: Int,
    val evolvePhase: Int,
    val level: Int,
    val equipLevel: Int,
    val enableRogueChar: Boolean
)