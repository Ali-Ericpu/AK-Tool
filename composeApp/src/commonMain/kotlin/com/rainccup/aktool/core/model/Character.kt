package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val instId: Int,
    val charId: String,
    val name: String? = null,
    val profession: String? = null,
    val rarity: Int? = null,
    val favorPoint: Int,
    val potentialRank: Int,
    val mainSkillLvl: Int,
    val skin: String,
    val level: Int,
    val exp: Int,
    val evolvePhase: Int,
    val defaultSkillIndex: Int,
    val gainTime: Long,
    val skills: List<Skill> = emptyList(),
    val voiceLan: String,
    val currentEquip: String? = null,
    val equip: Map<String, Equip> = emptyMap(),
    val starMark: Int,
    val currentTmpl: String? = null,
    val tmpl: Map<String, TmplChar>? = null,
) : Comparable<Character> {
    override fun compareTo(other: Character): Int =
        when {
            this.starMark != other.starMark -> other.starMark.compareTo(starMark)
            this.evolvePhase != other.evolvePhase -> other.evolvePhase.compareTo(evolvePhase)
            this.level != other.level -> other.level.compareTo(level)
            this.rarity != other.rarity -> other.rarity!!.compareTo(this.rarity!!)
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
            rarity = 5,
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
    val unlock: Int,
    val state: Int,
    val specializeLevel: Int,
    val completeUpgradeTime: Long,
)

@Serializable
data class Equip(
    val hide: Int,
    val level: Int,
    val locked: Int,
) {
    fun lock(): Equip = Equip(1, 1, 1)
    fun unlock(): Equip = Equip(0, 1, 0)
}

@Serializable
data class TmplChar(
    val skinId: String,
    val defaultSkillIndex: Int,
    val skills: List<Skill>,
    val currentEquip: String? = null,
    val equip: MutableMap<String, Equip>,
)
