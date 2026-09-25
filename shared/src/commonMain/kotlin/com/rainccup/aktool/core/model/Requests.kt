package com.rainccup.aktool.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveCharRequest(
    val charInstId: Int,
    val char: Character,
)

@Serializable
data class GainItemRequest(val items: List<Item>)

@Serializable
data class SaveStatusRequest(
    val nickName: String? = null,
    val nickNumber: String? = null,
    val level: Int? = null,
    val diamond: Int? = null,
    val diamondShard: Int? = null,
    val ap: Int? = null,
    val gold: Int? = null,
    val hggShard: Int? = null,
    val lggShard: Int? = null,
    val gachaTkt: Int? = null,
    val tenGachaTkt: Int? = null,
    val classicGachaTkt: Int? = null,
    val tenClassicGachaTkt: Int? = null,
    val classicShard: Int? = null,
    val tryTkt: Int? = null,
    val recTkt: Int? = null,
    val fniTkt: Int? = null,
)

@Serializable
data class UnlockAllCharRequest(
    val favorPoint: Int,
    val potentialRank: Int,
    val specializeLevel: Int,
    val mainSkillLvl: Int,
    val evolvePhase: Int,
    val level: Int,
    val equipLevel: Int,
    val enableRogueChar: Boolean,
)

@Serializable
data class AddFlushMessageRequest(
    val uid: String,
    val message: String,
)

@Serializable
data class ResetActivityRequest(
    val type: String,
    val id: String,
)

@Serializable
data class RegisterAccountRequest(
    val account: String,
    val password: String,
)
