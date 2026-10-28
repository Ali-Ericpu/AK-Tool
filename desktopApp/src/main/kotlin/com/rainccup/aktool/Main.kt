package com.rainccup.aktool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
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
import javax.imageio.ImageIO

fun main() {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(appModule) }
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AK Tool",
            icon = remember { loadWindowIcon() },
            state = rememberAdaptiveWindowState(),
        ) {
            App()
        }
    }
}

/**
 * 窗口图标：读取 desktopApp 资源里的 icon.png（由 assets/app-icon/generate_icon.py 生成）。
 * 读不到时返回 null，窗口用系统默认图标，不影响启动。
 */
private fun loadWindowIcon(): BitmapPainter? =
    object {}.javaClass.getResourceAsStream("/icon.png")
        ?.use { ImageIO.read(it) }
        ?.let { BitmapPainter(it.toComposeImageBitmap()) }

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
 * 主屏可用区域（已排除任务栏），单位就是 Compose 的 dp；取不到时返回 null。
 *
 * 只取主屏；多屏时窗口若被移到其它屏，这个上限只是近似值。
 */
private fun desktopWorkArea(): DpSize? = runCatching {
    val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
    if (bounds.width <= 0 || bounds.height <= 0) {
        null
    } else {
        DpSize((bounds.width * 0.8).dp, (bounds.height * 0.8).dp)
    }
}.getOrNull()
