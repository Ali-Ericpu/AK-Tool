package org.doctorate.aktool.pojo.entity

data class Character(
    val instId: Int,
    val charId: String,
    var name: String? = null,
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
    var currentEquip: String? = null,
    var equip: MutableMap<String, Equip>,
    var starMark: Int,
    var currentTmpl: String? = null,
    var tmpl: MutableMap<String, TmplChar>? = null
) : Comparable<Character> {
    override fun compareTo(other: Character): Int =
        when {
            this.starMark != other.starMark -> other.starMark.compareTo(starMark)
            this.evolvePhase != other.evolvePhase -> other.evolvePhase.compareTo(evolvePhase)
            this.level != other.level -> other.level.compareTo(level)
            this.rank != other.rank -> other.rank!!.compareTo(rank!!)
            this.profession != other.profession -> other.profession!!.compareTo(profession!!)
            this.name != other.name -> other.name!!.compareTo(name!!)
            else -> 0
        }
}

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