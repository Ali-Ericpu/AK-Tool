package org.doctorate.aktool.pojo.entity

import androidx.annotation.DrawableRes
import org.doctorate.aktool.R

sealed interface CharPainter {
    @get:DrawableRes
    val rarityPainter: Int

    @get:DrawableRes
    val charBgPainter: Int

    @get:DrawableRes
    val upperHubPainter: Int

    @get:DrawableRes
    val lowerHubPainter: Int

    @get:DrawableRes
    val rarityLightPainter: Int

    companion object {
        @JvmStatic
        fun form(rarity: Int): CharPainter {
            return when (rarity) {
                1 -> Rare1Char
                2 -> Rare2Char
                3 -> Rare3Char
                4 -> Rare4Char
                5 -> Rare5Char
                6 -> Rare6Char
                else -> throw RuntimeException("")
            }
        }
    }
}

object Rare1Char : CharPainter {
    override val rarityPainter: Int = R.drawable.character_star_1
    override val upperHubPainter: Int = R.drawable.character_upperhub_1
    override val rarityLightPainter: Int = R.drawable.character_rartylight_1
    override val charBgPainter: Int = R.drawable.character_charbg_1
    override val lowerHubPainter: Int = R.drawable.character_lowerhub1
}

object Rare2Char : CharPainter {
    override val rarityPainter: Int = R.drawable.character_star_2
    override val upperHubPainter: Int = R.drawable.character_upperhub_2
    override val rarityLightPainter: Int = R.drawable.character_rartylight_2
    override val charBgPainter: Int = R.drawable.character_charbg_1
    override val lowerHubPainter: Int = R.drawable.character_lowerhub1
}

object Rare3Char : CharPainter {
    override val rarityPainter: Int = R.drawable.character_star_3
    override val upperHubPainter: Int = R.drawable.character_upperhub_3
    override val rarityLightPainter: Int = R.drawable.character_rartylight_3
    override val charBgPainter: Int = R.drawable.character_charbg_1
    override val lowerHubPainter: Int = R.drawable.character_lowerhub1
}

object Rare4Char : CharPainter {
    override val rarityPainter: Int = R.drawable.character_star_4
    override val upperHubPainter: Int = R.drawable.character_upperhub_4
    override val rarityLightPainter: Int = R.drawable.character_rartylight_4
    override val charBgPainter: Int = R.drawable.character_charbg_4
    override val lowerHubPainter: Int = R.drawable.character_lowerhub4
}

object Rare5Char : CharPainter {
    override val rarityPainter: Int = R.drawable.character_star_5
    override val upperHubPainter: Int = R.drawable.character_upperhub_5
    override val rarityLightPainter: Int = R.drawable.character_rartylight_5
    override val charBgPainter: Int = R.drawable.character_charbg_5
    override val lowerHubPainter: Int = R.drawable.character_lowerhub5
}

object Rare6Char : CharPainter {
    override val rarityPainter: Int = R.drawable.character_star_6
    override val upperHubPainter: Int = R.drawable.character_upperhub_6
    override val rarityLightPainter: Int = R.drawable.character_rartylight_6
    override val charBgPainter: Int = R.drawable.character_charbg_6
    override val lowerHubPainter: Int = R.drawable.character_lowerhub6
}