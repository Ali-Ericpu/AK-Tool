package com.rainccup.aktool.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.Colors

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    secondary = secondaryDark,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    secondary = secondaryLight,
    tertiary = Pink40,
)

/**
 * Miuix Colors -> Material3 ColorScheme。
 * 取值优先级（spec S2）：Miuix 同名/同义 token -> 由已映射基础色派生 -> 保留 Color.kt 常量。
 */
fun bridgeToMaterial3(colors: Colors, dark: Boolean): ColorScheme {
    // 规则 1：Miuix 存在同名/同义 token，直接取用
    val primary = colors.primary
    val onPrimary = colors.onPrimary
    val primaryContainer = colors.primaryContainer
    val onPrimaryContainer = colors.onPrimaryContainer
    val secondary = colors.secondary
    val onSecondary = colors.onSecondary
    val secondaryContainer = colors.secondaryContainer
    val onSecondaryContainer = colors.onSecondaryContainer
    val tertiaryContainer = colors.tertiaryContainer
    val onTertiaryContainer = colors.onTertiaryContainer
    val error = colors.error
    val onError = colors.onError
    val errorContainer = colors.errorContainer
    val onErrorContainer = colors.onErrorContainer
    val background = colors.background
    val onBackground = colors.onBackground
    val surface = colors.surface
    val onSurface = colors.onSurface
    val surfaceVariant = colors.surfaceVariant
    val onSurfaceVariant = colors.onSurfaceVariantSummary
    val outline = colors.outline
    val outlineVariant = colors.dividerLine

    // 规则 3：Miuix 无 tertiary，沿用 Color.kt 既有 AKTool 值（原 Theme.kt 即用 Pink40/Pink80）
    val tertiary = if (dark) Pink80 else Pink40

    return if (dark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onSecondary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            // 规则 2：无对应 token 的槽位由已映射基础色派生
            surfaceTint = primary,
            inverseSurface = onBackground,
            inverseOnSurface = background,
            inversePrimary = primaryContainer,
            scrim = colors.windowDimming,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onSecondary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            surfaceTint = primary,
            inverseSurface = onBackground,
            inverseOnSurface = background,
            inversePrimary = primaryContainer,
            scrim = colors.windowDimming,
        )
    }
}

@Composable
fun AKToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // dynamicColor intentionally unused in common: Desktop has no Material You.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
