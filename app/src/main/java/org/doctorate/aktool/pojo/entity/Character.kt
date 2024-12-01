package org.doctorate.aktool.pojo.entity

import org.doctorate.aktool.R
import java.io.Serializable

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
) : Comparable<Character>, Serializable {
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

    companion object {
        fun char() = Character(
            1,
            "char_4080_lin",
            "林",
            "CASTER",
            6,
            25570,
            5,
            7,
            "char_4080_lin#2",
            90,
            0,
            2,
            2,
            1700000000L,
            listOf(
                Skill(
                    state = 0,
                    skillId = "skchr_lin_1",
                    unlock = 1,
                    specializeLevel = 3,
                    completeUpgradeTime = -1
                ),
                Skill(
                    state = 0,
                    skillId = "skchr_lin_2",
                    unlock = 1,
                    specializeLevel = 3,
                    completeUpgradeTime = -1
                ),
                Skill(
                    state = 0,
                    skillId = "skchr_lin_3",
                    unlock = 1,
                    specializeLevel = 3,
                    completeUpgradeTime = -1
                ),
            ),
            "JP",
            null,
            mutableMapOf(),
            1,
            null,
            null
        )
    }
}

data class Skill(
    val skillId: String,
    var unlock: Int,
    var state: Int,
    var specializeLevel: Int,
    var completeUpgradeTime: Long,
) : Serializable

data class Equip(
    var hide: Int,
    var level: Int,
    var locked: Int
) : Serializable {
    fun lock() {
        hide = 1
        level = 1
        locked = 1
    }

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
) : Serializable

enum class Profession(val icon: Int) {
    SNIPER(R.drawable.character_profession_sniper),
    WARRIOR(R.drawable.character_profession_warrior),
    TANK(R.drawable.character_profession_tank),
    PIONEER(R.drawable.character_profession_pioneer),
    CASTER(R.drawable.character_profession_caster),
    MEDIC(R.drawable.character_profession_medic),
    SUPPORT(R.drawable.character_profession_support),
    SPECIAL(R.drawable.character_profession_special),
}