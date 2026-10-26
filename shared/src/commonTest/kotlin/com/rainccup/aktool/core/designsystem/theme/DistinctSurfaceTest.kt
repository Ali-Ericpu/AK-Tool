package com.rainccup.aktool.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import top.yukonga.miuix.kmp.theme.lightColorScheme

class DistinctSurfaceTest {

    /** Material You 里 surface 与 background 同色，这里必须被拆成两个 tone。 */
    @Test
    fun monetCollisionIsSeparated() {
        val monet = lightColorScheme().copy(
            background = Color(0xFFFCF8F8),
            surface = Color(0xFFFCF8F8),
        )

        val colors = monet.withDistinctSurface(dark = false)

        assertNotEquals(colors.background, colors.surface)
        // 浅色下沉，和固定色板里 F7F7F7 / White 的关系一致
        assertTrue(colors.surface.luminance() < colors.background.luminance())
        // 差距要看得出来：至少一个通道差 5%
        assertTrue(colors.background.red - colors.surface.red > 0.05f)
        assertEquals(monet.background, colors.background, "background 不该被改动")
    }

    @Test
    fun darkSurfaceFloatsAboveBackground() {
        val monet = lightColorScheme().copy(
            background = Color(0xFF141212),
            surface = Color(0xFF141212),
        )

        val colors = monet.withDistinctSurface(dark = true)

        assertNotEquals(colors.background, colors.surface)
        // 深色上浮，和固定色板里 #1A1A1A / Black 的关系一致
        assertTrue(colors.surface.luminance() > colors.background.luminance())
        assertTrue(colors.surface.red - colors.background.red > 0.05f)
        assertEquals(monet.background, colors.background, "background 不该被改动")
    }

    /** 固定色板本来就把两者分开了，必须原样返回。 */
    @Test
    fun alreadyDistinctPaletteIsUntouched() {
        val fixed = lightColorScheme()

        assertSame(fixed, fixed.withDistinctSurface(dark = false))
        assertSame(fixed, fixed.withDistinctSurface(dark = true))
    }

    /** 新的 surface 仍是实色，覆盖到页面上不会变透。 */
    @Test
    fun separatedSurfaceIsOpaque() {
        val monet = lightColorScheme().copy(
            background = Color(0xFFFCF8F8),
            surface = Color(0xFFFCF8F8),
        )

        val surface = monet.withDistinctSurface(dark = false).surface

        assertEquals(1f, surface.alpha)
        assertEquals(surface, surface.copy(alpha = 0.5f).compositeOver(surface))
    }
}
