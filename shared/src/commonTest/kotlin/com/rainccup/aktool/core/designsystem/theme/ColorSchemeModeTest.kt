package com.rainccup.aktool.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ColorSchemeModeTest {

    @Test
    fun withoutCustomColor_keepsSpecS2Priority() {
        // darkMode > dynamicColor > System
        assertEquals(
            ColorSchemeMode.Dark,
            resolveColorSchemeMode(darkMode = true, dynamicColor = true, hasCustomColor = false),
        )
        assertEquals(
            ColorSchemeMode.MonetSystem,
            resolveColorSchemeMode(darkMode = false, dynamicColor = true, hasCustomColor = false),
        )
        assertEquals(
            ColorSchemeMode.System,
            resolveColorSchemeMode(darkMode = false, dynamicColor = false, hasCustomColor = false),
        )
    }

    @Test
    fun dynamicColorOff_neverGoesMonet() {
        // 关掉开关就不走 Monet 映射，哪怕已经选了主题色
        assertEquals(
            ColorSchemeMode.System,
            resolveColorSchemeMode(darkMode = false, dynamicColor = false, hasCustomColor = true),
        )
        assertEquals(
            ColorSchemeMode.Dark,
            resolveColorSchemeMode(darkMode = true, dynamicColor = false, hasCustomColor = true),
        )
    }

    @Test
    fun customColor_forcesMonetWhileDynamicColorIsOn() {
        // 开启动态色彩且有自选色时必须进 Monet，否则 keyColor 无处生效
        assertEquals(
            ColorSchemeMode.MonetSystem,
            resolveColorSchemeMode(darkMode = false, dynamicColor = true, hasCustomColor = true),
        )
        assertEquals(
            ColorSchemeMode.MonetDark,
            resolveColorSchemeMode(darkMode = true, dynamicColor = true, hasCustomColor = true),
        )
    }

    @Test
    fun seedIsNullOnlyForTheSentinel() {
        assertNull(seedColorOrNull(UNSET_PRIMARY_COLOR))
        assertEquals(Color(0xFF1E88E5), seedColorOrNull(Color(0xFF1E88E5).value))
    }

    @Test
    fun seedRoundTripsTheValuePersistedBySettingPage() {
        // 设置页写的是 draftColor.value，而 Color 的 value 就是打包后的原始值，
        // 所以持久化再还原必须是无损的
        val picked = Color(0xFF1E88E5)
        assertEquals(0xFF1E88E500000000UL, picked.value)
        assertEquals(picked, seedColorOrNull(picked.value))
    }

    @Test
    fun sentinelWouldOtherwiseBeATransparentBlackSeed() {
        // 说明为什么必须拦掉哨兵：0x0UL 是全透明黑，而不是 Compose 的 Unspecified
        assertEquals(0, Color(UNSET_PRIMARY_COLOR).toArgb())
        assertFalse(Color(UNSET_PRIMARY_COLOR).isUnspecified)
    }

    @Test
    fun directColors_useThePickedColorVerbatim() {
        // 不走 Monet 映射：primary 家族等于所选颜色本身，不生成色调板
        val base = miuixLightColorScheme()
        val picked = Color(0xFF1E88E5)
        val direct = directPrimaryColors(base, picked)

        assertEquals(picked, direct.primary)
        assertEquals(picked, direct.primaryVariant)
        assertEquals(picked, direct.primaryContainer)
        assertEquals(picked, direct.sliderKeyPoint)
        assertEquals(picked, direct.onBackgroundVariant)
    }

    @Test
    fun directColors_leaveTheRestOfTheFixedPaletteAlone() {
        val base = miuixLightColorScheme()
        val direct = directPrimaryColors(base, Color(0xFF1E88E5))

        assertEquals(base.background, direct.background)
        assertEquals(base.surface, direct.surface)
        assertEquals(base.onSurface, direct.onSurface)
        assertEquals(base.secondary, direct.secondary)
        assertEquals(base.tertiaryContainer, direct.tertiaryContainer)
        assertEquals(base.error, direct.error)
        assertEquals(base.outline, direct.outline)
    }

    @Test
    fun directColors_flattenDisabledAndSliderSlotsLikeMiuixDoes() {
        // 唯一必须算的派生槽位：沿用 Miuix MonetMapping 的压平方式
        val base = miuixDarkColorScheme()
        val picked = Color(0xFF1E88E5)
        val direct = directPrimaryColors(base, picked)
        val disabledPrimary = picked.copy(alpha = 0.38f).compositeOver(base.surface)

        assertEquals(disabledPrimary, direct.disabledPrimary)
        assertEquals(disabledPrimary, direct.disabledPrimaryButton)
        assertEquals(disabledPrimary, direct.disabledPrimarySlider)
        assertEquals(
            direct.onPrimary.copy(alpha = 0.38f).compositeOver(disabledPrimary),
            direct.disabledOnPrimary,
        )
        assertEquals(
            direct.onPrimary.copy(alpha = 0.6f).compositeOver(disabledPrimary),
            direct.disabledOnPrimaryButton,
        )
        assertEquals(picked.copy(alpha = 0.2f).compositeOver(base.surface), direct.sliderBackground)
    }

    @Test
    fun contrastingOn_alwaysPicksTheHigherContrastSide() {
        fun contrastRatio(a: Color, b: Color): Float {
            val hi = maxOf(a.luminance(), b.luminance())
            val lo = minOf(a.luminance(), b.luminance())
            return (hi + 0.05f) / (lo + 0.05f)
        }

        val backgrounds = listOf(
            Color.Black,
            Color.White,
            Color(0xFF1E88E5),
            Color(0xFF0D47A1),
            Color(0xFFFFF176),
        )
        for (background in backgrounds) {
            val on = contrastingOn(background)
            val other = if (on == Color.White) Color.Black else Color.White
            assertTrue(
                contrastRatio(on, background) >= contrastRatio(other, background),
                "expected the higher-contrast on-color for $background",
            )
        }
        assertEquals(Color.White, contrastingOn(Color.Black))
        assertEquals(Color.Black, contrastingOn(Color.White))
    }
}
