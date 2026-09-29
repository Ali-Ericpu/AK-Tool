package com.rainccup.aktool.feature.character.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 首页干员网格的布局尺寸。
 */
object CharacterGridMetrics {

    /** 单张卡片的布局尺寸，`CharacterCard` 与网格列宽都用它。 */
    val CardWidth: Dp = 108.dp
    val CardHeight: Dp = 228.dp

    /** 一行至少要能放下的卡片数；桌面默认窗口宽度就是按它反推的。 */
    const val ColumnsPerRow: Int = 3

    /**
     * 窗口内容区左右被占掉的宽度：`AppNavHost` 里 `NavDisplay` 的 12dp 内边距，左右各一份。
     * 网格自身没有 contentPadding，所以这是唯一的横向扣减。
     */
    val HorizontalChrome: Dp = 24.dp

    /**
     * 在窗口坐标系下放下 [columns] 列卡片所需的宽度。
     *
     * 卡片写的是 dp，但页面把 `LocalDensity` 乘了 `platformUiScale()`，
     * 所以一张卡在窗口里实际占 [CardWidth] × [uiScale]。
     */
    fun widthFor(columns: Int, uiScale: Float): Dp =
        CardWidth * columns * uiScale + HorizontalChrome

    /**
     * 默认窗口宽度：正好放下 [ColumnsPerRow] 列，再多留半列空隙交给 `SpaceAround` 分布
     * （不留空隙的话卡片会紧贴内边距）。
     *
     * 半列是刻意的安全余量：要多显示一列必须再多出整整一张卡的宽度，所以「多给半列」既留出
     * 了分布用的缝，又不足以让列数发生变化——对任何 [uiScale] 都成立。
     */
    fun defaultWindowWidth(uiScale: Float): Dp =
        widthFor(ColumnsPerRow, uiScale) + CardWidth * uiScale / 2
}
