package org.doctorate.aktool.pojo.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
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