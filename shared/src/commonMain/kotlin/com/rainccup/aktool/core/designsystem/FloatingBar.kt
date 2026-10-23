package com.rainccup.aktool.core.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 悬浮底栏在内容区底部遮住的高度（不含系统导航栏那一段）。
 *
 * 内容区一直铺到安全区底边、从底栏底下穿过去（玻璃要采样到滑过去的内容才有质感），
 * 所以"最后一项别被浮层永久压住"这件事只能交给各页面的滚动容器：
 * 把它加进 `contentPadding` 的 bottom，滚到底时最后一项正好停在浮层之上。
 *
 * 用 `compositionLocalOf` 而不是 `staticCompositionLocalOf`：底栏高度会随系统栏、字号变化，
 * 只需要让读到它的滚动容器重新布局，不必整棵子树重组。
 *
 * 左侧导航栏的大屏布局、以及详情页这类没有底栏的页面，取到的是 0.dp。
 */
val LocalFloatingBarInset = compositionLocalOf { 0.dp }

/**
 * 悬浮底栏给 Scaffold 带来的两个尺寸，都由 [floatingBarMetrics] 从同一份 `paddingValues`
 * 算出，省得两处各算一遍、再算出不一样的结果。
 */
@Immutable
data class FloatingBarMetrics(
    /** 页面内容该吃的内边距：左右与顶部沿用 Scaffold，底部只让开系统栏。 */
    val contentPadding: PaddingValues,
    /** 底栏真正压住内容的高度，见 [LocalFloatingBarInset]。 */
    val reserve: Dp,
)

/**
 * 把 `Scaffold` 给的 [scaffoldPadding] 拆成「内容内边距」与「底栏占位」。
 *
 * 底栏挂在 Scaffold 的 `bottomBar` 槽里，所以 `paddingValues.bottom` 就是它的实测高度；
 * 而 `FloatingNavigationBar` 自己会带上系统导航栏那一段（miuix 在组件内部取
 * `navigationBars` 的底部内边距），内容本来也让开了系统栏，因此要扣掉它，
 * 剩下的才是底栏真正压住内容的高度。两者相加正好还原 `paddingValues.bottom`。
 *
 * 底栏收起时 `paddingValues.bottom` 回落成系统栏高度，[FloatingBarMetrics.reserve] 自然归零，
 * 大屏的左侧导航栏与详情页因此都不会多留白。
 */
@Composable
fun floatingBarMetrics(scaffoldPadding: PaddingValues): FloatingBarMetrics {
    val barBlock = scaffoldPadding.calculateBottomPadding()
    val systemBottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val layoutDirection = LocalLayoutDirection.current
    val reserve = (barBlock - systemBottom).coerceAtLeast(0.dp)
    return FloatingBarMetrics(
        contentPadding = PaddingValues(
            start = scaffoldPadding.calculateStartPadding(layoutDirection),
            top = scaffoldPadding.calculateTopPadding(),
            end = scaffoldPadding.calculateEndPadding(layoutDirection),
            // 内容穿到底栏下面：底部只吃系统栏，不吃底栏高度。
            bottom = barBlock - reserve,
        ),
        reserve = reserve,
    )
}
