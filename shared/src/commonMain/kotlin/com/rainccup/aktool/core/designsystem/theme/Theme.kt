package com.rainccup.aktool.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

/**
 * AppConfig.primaryColor 的「未设置」哨兵值：0 表示用户没有自选主题色，
 * 此时配色模式回退到 darkMode / dynamicColor 的既有优先级。
 *
 * 注意它并不是 Compose 的 [Color.Unspecified]（后者的原始值是 0x10UL），
 * 而是一个全透明黑；若直接拿去当种子会生成一整套灰黑色板，所以必须在这里拦掉。
 */
const val UNSET_PRIMARY_COLOR: ULong = 0x0UL

/**
 * 把持久化的 [Color.value] 还原成 [Color]；未设置时返回 null，
 * 让 Miuix 在 Monet 模式下回退到平台动态取色（Android 12+ 的壁纸取色）。
 */
fun seedColorOrNull(primaryColor: ULong): Color? =
    if (primaryColor == UNSET_PRIMARY_COLOR) null else Color(primaryColor)

/**
 * 解析 Miuix 配色模式。
 *
 * [dynamicColor] 是总开关：关闭时完全不进 Monet，只用固定色板（自选色由
 * [directPrimaryColors] 直出到 primary 家族）。开启时没有自选色就沿用 spec S2 的
 * darkMode > dynamicColor > System；一旦自选了主题色就进入 Monet，否则 keyColor 无处生效。
 */
fun resolveColorSchemeMode(
    darkMode: Boolean,
    dynamicColor: Boolean,
    hasCustomColor: Boolean,
): ColorSchemeMode = when {
    !dynamicColor -> if (darkMode) ColorSchemeMode.Dark else ColorSchemeMode.System
    hasCustomColor -> if (darkMode) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetSystem
    darkMode -> ColorSchemeMode.Dark
    else -> ColorSchemeMode.MonetSystem
}

/**
 * 「直出」色板：不走 Monet 映射时，在固定色板上把自选色原样盖到 primary 家族。
 *
 * primary/primaryVariant/primaryContainer 都刻意等于所选颜色本身（不生成色调板），
 * 只有两类槽位无法直出、必须算：[contrastingOn] 保证压在 primary 上的文字可读，
 * disabled 系列与 sliderBackground 则沿用 Miuix 自己 `MonetMapping` 的做法——把带透明度的颜色压到 surface 上。
 */
fun directPrimaryColors(base: Colors, primary: Color): Colors {
    // Miuix 对 disabledPrimary / disabledPrimaryButton / disabledPrimarySlider 用的是同一个压平结果
    val disabledPrimary = primary.copy(alpha = 0.38f).compositeOver(base.surface)
    val onPrimary = contrastingOn(primary)
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryVariant = primary,
        onPrimaryVariant = onPrimary,
        primaryContainer = primary,
        onPrimaryContainer = onPrimary,
        disabledPrimary = disabledPrimary,
        disabledOnPrimary = onPrimary.copy(alpha = 0.38f).compositeOver(disabledPrimary),
        disabledPrimaryButton = disabledPrimary,
        disabledOnPrimaryButton = onPrimary.copy(alpha = 0.6f).compositeOver(disabledPrimary),
        disabledPrimarySlider = disabledPrimary,
        sliderBackground = primary.copy(alpha = 0.2f).compositeOver(base.surface),
        sliderKeyPoint = primary,
        onBackgroundVariant = primary,
    )
}

/** 在黑白之间挑与 [background] 对比度更高的那个，用于压在它上面的 on* 文字色。 */
fun contrastingOn(background: Color): Color {
    val luminance = background.luminance()
    val againstBlack = (luminance + 0.05f) / 0.05f
    val againstWhite = 1.05f / (luminance + 0.05f)
    return if (againstBlack >= againstWhite) Color.Black else Color.White
}

@Composable
fun AKToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Miuix 负责配色模式（Monet 动态取色或直出的固定色板）；这里只把当前 Miuix 色板桥接给 Material3
    MaterialTheme(
        colorScheme = bridgeToMaterial3(MiuixTheme.colorScheme, darkTheme),
        typography = Typography,
        content = content,
    )
}
