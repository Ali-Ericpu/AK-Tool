package org.doctorate.aktool.pojo.entity

data class Character(
    val instId: Int,
    val charId: String,
    var profession: String? = null,
    var rank: Int? = null,
    var favorPoint: Int,
    var potentialRank: Int,
    var mainSkillLvl: Int,
    var skin: String,
    var level: Int,
    var exp: Int,
    var evolvePhase: Int,
    var defaultSkillIndex: Int,
    var gainTime: Long,
    var skills: List<Skill>,
    var voiceLan: String,
    var currentEquip: String?,
    var equip: MutableMap<String, Equip>,
    var starMark: Int,
    var currentTmpl: String?,
    var tmpl: MutableMap<String, TmplChar>?
)

data class Skill(
    val skillId: String,
    var unlock: Int,
    var state: Int,
    var specializeLevel: Int,
    var completeUpgradeTime: Long,
)

data class Equip(
    var hide: Int,
    var level: Int,
    var locked: Int
) {
    fun unlock() {
        hide = 0
        level = 1
        locked = 0
    }
}

data class TmplChar(
    var skinId: String,
    var defaultSkillIndex: Int,
    var skills: List<Skill>,
    var currentEquip: String?,
    var equip: MutableMap<String, Equip>
)