package com.rainccup.aktool.core.designsystem.component

import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.character_charbg_1
import com.rainccup.aktool.resources.character_charbg_4
import com.rainccup.aktool.resources.character_charbg_5
import com.rainccup.aktool.resources.character_charbg_6
import com.rainccup.aktool.resources.character_lowerhub1
import com.rainccup.aktool.resources.character_lowerhub4
import com.rainccup.aktool.resources.character_lowerhub5
import com.rainccup.aktool.resources.character_lowerhub6
import com.rainccup.aktool.resources.character_rartylight_1
import com.rainccup.aktool.resources.character_rartylight_2
import com.rainccup.aktool.resources.character_rartylight_3
import com.rainccup.aktool.resources.character_rartylight_4
import com.rainccup.aktool.resources.character_rartylight_5
import com.rainccup.aktool.resources.character_rartylight_6
import com.rainccup.aktool.resources.character_star_1
import com.rainccup.aktool.resources.character_star_2
import com.rainccup.aktool.resources.character_star_3
import com.rainccup.aktool.resources.character_star_4
import com.rainccup.aktool.resources.character_star_5
import com.rainccup.aktool.resources.character_star_6
import com.rainccup.aktool.resources.character_upperhub_1
import com.rainccup.aktool.resources.character_upperhub_2
import com.rainccup.aktool.resources.character_upperhub_3
import com.rainccup.aktool.resources.character_upperhub_4
import com.rainccup.aktool.resources.character_upperhub_5
import com.rainccup.aktool.resources.character_upperhub_6
import org.jetbrains.compose.resources.DrawableResource

sealed interface CharPainter {
    val rarityPainter: DrawableResource
    val charBgPainter: DrawableResource
    val upperHubPainter: DrawableResource
    val lowerHubPainter: DrawableResource
    val rarityLightPainter: DrawableResource

    companion object {
        fun form(rarity: Int): CharPainter = when (rarity) {
            1 -> Rare1Char
            2 -> Rare2Char
            3 -> Rare3Char
            4 -> Rare4Char
            5 -> Rare5Char
            6 -> Rare6Char
            else -> Rare1Char
        }
    }
}

object Rare1Char : CharPainter {
    override val rarityPainter = Res.drawable.character_star_1
    override val upperHubPainter = Res.drawable.character_upperhub_1
    override val rarityLightPainter = Res.drawable.character_rartylight_1
    override val charBgPainter = Res.drawable.character_charbg_1
    override val lowerHubPainter = Res.drawable.character_lowerhub1
}

object Rare2Char : CharPainter {
    override val rarityPainter = Res.drawable.character_star_2
    override val upperHubPainter = Res.drawable.character_upperhub_2
    override val rarityLightPainter = Res.drawable.character_rartylight_2
    override val charBgPainter = Res.drawable.character_charbg_1
    override val lowerHubPainter = Res.drawable.character_lowerhub1
}

object Rare3Char : CharPainter {
    override val rarityPainter = Res.drawable.character_star_3
    override val upperHubPainter = Res.drawable.character_upperhub_3
    override val rarityLightPainter = Res.drawable.character_rartylight_3
    override val charBgPainter = Res.drawable.character_charbg_1
    override val lowerHubPainter = Res.drawable.character_lowerhub1
}

object Rare4Char : CharPainter {
    override val rarityPainter = Res.drawable.character_star_4
    override val upperHubPainter = Res.drawable.character_upperhub_4
    override val rarityLightPainter = Res.drawable.character_rartylight_4
    override val charBgPainter = Res.drawable.character_charbg_4
    override val lowerHubPainter = Res.drawable.character_lowerhub4
}

object Rare5Char : CharPainter {
    override val rarityPainter = Res.drawable.character_star_5
    override val upperHubPainter = Res.drawable.character_upperhub_5
    override val rarityLightPainter = Res.drawable.character_rartylight_5
    override val charBgPainter = Res.drawable.character_charbg_5
    override val lowerHubPainter = Res.drawable.character_lowerhub5
}

object Rare6Char : CharPainter {
    override val rarityPainter = Res.drawable.character_star_6
    override val upperHubPainter = Res.drawable.character_upperhub_6
    override val rarityLightPainter = Res.drawable.character_rartylight_6
    override val charBgPainter = Res.drawable.character_charbg_6
    override val lowerHubPainter = Res.drawable.character_lowerhub6
}
