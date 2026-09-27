package com.rainccup.aktool.core.designsystem

import com.rainccup.aktool.core.model.Profession
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.character_profession_caster
import com.rainccup.aktool.resources.character_profession_medic
import com.rainccup.aktool.resources.character_profession_pioneer
import com.rainccup.aktool.resources.character_profession_sniper
import com.rainccup.aktool.resources.character_profession_special
import com.rainccup.aktool.resources.character_profession_support
import com.rainccup.aktool.resources.character_profession_tank
import com.rainccup.aktool.resources.character_profession_warrior
import org.jetbrains.compose.resources.DrawableResource

/** 职业图标属于表现层，放在设计系统里；模型层只保留纯枚举。 */
val Profession.icon: DrawableResource
    get() = when (this) {
        Profession.SNIPER -> Res.drawable.character_profession_sniper
        Profession.WARRIOR -> Res.drawable.character_profession_warrior
        Profession.TANK -> Res.drawable.character_profession_tank
        Profession.PIONEER -> Res.drawable.character_profession_pioneer
        Profession.CASTER -> Res.drawable.character_profession_caster
        Profession.MEDIC -> Res.drawable.character_profession_medic
        Profession.SUPPORT -> Res.drawable.character_profession_support
        Profession.SPECIAL -> Res.drawable.character_profession_special
    }
