package com.rainccup.aktool.core.model

import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.icon_profession_caster
import com.rainccup.aktool.resources.icon_profession_medic
import com.rainccup.aktool.resources.icon_profession_pioneer
import com.rainccup.aktool.resources.icon_profession_sniper
import com.rainccup.aktool.resources.icon_profession_special
import com.rainccup.aktool.resources.icon_profession_support
import com.rainccup.aktool.resources.icon_profession_tank
import com.rainccup.aktool.resources.icon_profession_warrior
import org.jetbrains.compose.resources.DrawableResource

enum class Profession(val icon: DrawableResource) {
    SNIPER(Res.drawable.icon_profession_sniper),
    WARRIOR(Res.drawable.icon_profession_warrior),
    TANK(Res.drawable.icon_profession_tank),
    PIONEER(Res.drawable.icon_profession_pioneer),
    CASTER(Res.drawable.icon_profession_caster),
    MEDIC(Res.drawable.icon_profession_medic),
    SUPPORT(Res.drawable.icon_profession_support),
    SPECIAL(Res.drawable.icon_profession_special),
}
