package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
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
    var skills: List<Skill> = emptyList(),
    var voiceLan: String,
    var currentEquip: String? = null,
    var equip: MutableMap<String, Equip> = mutableMapOf(),
    var starMark: Int,
    var currentTmpl: String? = null,
    var tmpl: MutableMap<String, TmplChar>? = null,
) : Comparable<Character> {
    override fun compareTo(other: Character): Int =
        when {
            this.starMark != other.starMark -> other.starMark.compareTo(starMark)
            this.evolvePhase != other.evolvePhase -> other.evolvePhase.compareTo(evolvePhase)
            this.level != other.level -> other.level.compareTo(level)
            this.rank != other.rank -> other.rank!!.compareTo(this.rank!!)
            this.profession != other.profession -> this.profession!!.compareTo(other.profession!!)
            this.name != other.name -> this.name!!.compareTo(other.name!!)
            else -> 0
        }

    companion object {
        fun placeholder() = Character(
            instId = 1,
            charId = "char_002_amiya",
            name = "阿米娅",
            profession = "CASTER",
            rank = 5,
            favorPoint = 0,
            potentialRank = 0,
            mainSkillLvl = 1,
            skin = "char_002_amiya#1",
            level = 1,
            exp = 0,
            evolvePhase = 0,
            defaultSkillIndex = 0,
            gainTime = 0L,
            skills = emptyList(),
            voiceLan = "CN",
            equip = mutableMapOf(),
            starMark = 0,
        )
    }
}

@Serializable
data class Skill(
    val skillId: String,
    var unlock: Int,
    var state: Int,
    var specializeLevel: Int,
    var completeUpgradeTime: Long,
)

@Serializable
data class Equip(
    var hide: Int,
    var level: Int,
    var locked: Int,
) {
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

@Serializable
data class TmplChar(
    var skinId: String,
    var defaultSkillIndex: Int,
    var skills: List<Skill>,
    var currentEquip: String? = null,
    var equip: MutableMap<String, Equip>,
)
