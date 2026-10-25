package com.rainccup.aktool.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.rainccup.aktool.core.common.LocalAppConfig
import com.rainccup.aktool.core.designsystem.liquid.FloatingGlassBar
import com.rainccup.aktool.core.designsystem.liquid.GlassBarTab
import com.rainccup.aktool.core.navigation.AppRoute
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.blur.Backdrop

/** 一个顶层标签页：路由 + 文案 + 图标。 */
internal data class TabMeta(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

/**
 * 四个顶层标签页。文案由调用方从资源里取，这样切换语言时会跟着重组。
 */
internal fun appTabs(
    home: String,
    character: String,
    extra: String,
    setting: String,
): List<TabMeta> = listOf(
    TabMeta(AppRoute.Home, home, Icons.Default.Home),
    TabMeta(AppRoute.Character, character, Icons.Default.People),
    TabMeta(AppRoute.Extra, extra, Icons.Default.Build),
    TabMeta(AppRoute.Setting, setting, Icons.Default.Settings),
)

/**
 * 底部悬浮玻璃底栏。
 *
 * 只负责摆放与进出场动画：底栏本体（玻璃、滑动选中块）都在 [FloatingGlassBar] 里，
 * 这样换布局时不必碰导航逻辑。
 */
@Composable
internal fun AppBottomBar(
    visible: Boolean,
    tabs: List<TabMeta>,
    currentTop: NavKey?,
    backdrop: Backdrop,
    onTabSelect: (AppRoute) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // 系统导航栏的留白由底栏自己让出来（以前是 miuix FloatingNavigationBar 内部做的）。
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val barTabs = remember(tabs) { tabs.map { GlassBarTab(it.label, it.icon) } }
            val selectedIndex = tabs.indexOfFirst { it.route == currentTop }
            FloatingGlassBar(
                selectedIndex = if (selectedIndex < 0) 0 else selectedIndex,
                onSelected = { index ->
                    tabs.getOrNull(index)?.let { onTabSelect(it.route) }
                },
                tabs = barTabs,
                backdrop = backdrop,
                isBlurEnabled = LocalAppConfig.current.config.liquidGlass,
            )
        }
    }
}

/** 宽屏下的左侧导航栏，与底部标签栏共用同一份 tabs 与选中逻辑。 */
@Composable
internal fun AppNavigationRail(
    tabs: List<TabMeta>,
    currentTop: NavKey?,
    onTabSelect: (AppRoute) -> Unit,
) {
    NavigationRail(
        color = Color.Transparent,
        showDivider = false,
    ) {
        tabs.forEach { tab ->
            NavigationRailItem(
                selected = currentTop == tab.route,
                onClick = { onTabSelect(tab.route) },
                icon = tab.icon,
                label = tab.label,
            )
        }
    }
}