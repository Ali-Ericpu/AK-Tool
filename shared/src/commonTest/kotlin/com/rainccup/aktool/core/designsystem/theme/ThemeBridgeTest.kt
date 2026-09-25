package com.rainccup.aktool.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeBridgeTest {

    @Test
    fun bridge_lightMapsMiuixTokensOntoMaterial3() {
        val miuix = miuixLightColorScheme()
        val m3: ColorScheme = bridgeToMaterial3(miuix, dark = false)

        assertEquals(miuix.primary, m3.primary)
        assertEquals(miuix.onPrimary, m3.onPrimary)
        assertEquals(miuix.primaryContainer, m3.primaryContainer)
        assertEquals(miuix.secondary, m3.secondary)
        assertEquals(miuix.secondaryContainer, m3.secondaryContainer)
        assertEquals(miuix.error, m3.error)
        assertEquals(miuix.onError, m3.onError)
        assertEquals(miuix.background, m3.background)
        assertEquals(miuix.onBackground, m3.onBackground)
        assertEquals(miuix.surface, m3.surface)
        assertEquals(miuix.onSurface, m3.onSurface)
        assertEquals(miuix.surfaceVariant, m3.surfaceVariant)
        assertEquals(miuix.onSurfaceVariantSummary, m3.onSurfaceVariant)
        assertEquals(miuix.outline, m3.outline)
        assertEquals(miuix.dividerLine, m3.outlineVariant)
    }

    @Test
    fun bridge_lightKeepsExistingAkToolTertiary() {
        val m3 = bridgeToMaterial3(miuixLightColorScheme(), dark = false)
        assertEquals(Pink40, m3.tertiary)
    }

    @Test
    fun bridge_darkMapsMiuixTokensAndKeepsAkToolTertiary() {
        val miuix = miuixDarkColorScheme()
        val m3 = bridgeToMaterial3(miuix, dark = true)

        assertEquals(miuix.primary, m3.primary)
        assertEquals(miuix.background, m3.background)
        assertEquals(miuix.onBackground, m3.onBackground)
        assertEquals(miuix.surface, m3.surface)
        assertEquals(Pink80, m3.tertiary)
    }

    @Test
    fun bridge_derivesInverseColorsFromMappedBase() {
        val miuix = miuixLightColorScheme()
        val m3 = bridgeToMaterial3(miuix, dark = false)

        assertEquals(miuix.onBackground, m3.inverseSurface)
        assertEquals(miuix.background, m3.inverseOnSurface)
        assertEquals(miuix.primaryContainer, m3.inversePrimary)
        assertEquals(miuix.windowDimming, m3.scrim)
        assertEquals(miuix.primary, m3.surfaceTint)
    }
}
