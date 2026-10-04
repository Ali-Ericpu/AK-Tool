package com.rainccup.aktool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rainccup.aktool.app.App
import com.rainccup.aktool.app.appModule
import com.rainccup.aktool.core.platform.platformUiScale
import com.rainccup.aktool.feature.character.ui.CharacterGridMetrics
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import java.awt.GraphicsEnvironment

fun main() {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(appModule) }
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AK Tool",
            state = rememberAdaptiveWindowState(),
        ) {
            App()
        }
    }
}

/** 默认窗口占屏幕可用宽度的比例。 */
private const val WindowWidthRatio = 0.9f

/** 默认窗口"高度 / 宽度"的上限（约 16:10）：屏幕再方，也保证默认开出来是横窗。 */
private const val MaxHeightToWidthRatio = 0.625f

/**
 * 默认窗口尺寸：**横屏**，按屏幕可用区域取比例。
 *
 * - 宽度取可用宽度的 [WindowWidthRatio]，
 * 并保底「一行放得下 [CharacterGridMetrics.ColumnsPerRow]张干员卡」——低于这个宽度网格会掉到 2 列；
 * - 高度取可用高度，但不超过宽度的 [MaxHeightToWidthRatio]，所以无论屏幕什么形状都是横窗；
 * - 这个宽度通常远超 600dp，因此桌面默认就会用上大屏的左侧导航栏。
 */
@Composable
private fun rememberAdaptiveWindowState(): WindowState {
    val workArea = remember { desktopWorkArea() }
    val minWidth = CharacterGridMetrics.defaultWindowWidth(platformUiScale())
    val width = workArea
        ?.let { (it.width * WindowWidthRatio).coerceAtLeast(minWidth) }
        ?: (minWidth * 2)
    val maxHeight = width * MaxHeightToWidthRatio
    val height = (workArea?.height ?: maxHeight).coerceAtMost(maxHeight)
    return rememberWindowState(
        width = width,
        height = height,
        position = WindowPosition(Alignment.Center)
    )
}

/**
 * 主屏可用区域（已排除任务栏），换算成 Compose 的 dp；取不到时返回 null。
 *
 * `maximumWindowBounds` 给的是屏幕像素，要按窗口缩放系数换算成 dp——
 * `defaultTransform.scaleX` 正是 Compose Desktop 用来做 dp→px 的那个系数。
 * 不除它的话，150% 缩放下算出来的可用区会偏大 1.5 倍，收敛就形同虚设。
 *
 * 只取主屏；多屏时窗口若被移到其它屏，这个上限只是近似值。
 */
private fun desktopWorkArea(): DpSize? = runCatching {
    val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val scale = environment.defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX
    val bounds = environment.maximumWindowBounds
    if (scale <= 0f || bounds.width <= 0 || bounds.height <= 0) {
        null
    } else {
        val width = bounds.width / scale
        val height = bounds.height / scale
        DpSize(width.dp, height.dp)
    }
}.getOrNull()
