package com.rainccup.aktool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
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

/**
 * 默认窗口尺寸：宽度按「一行放得下 [CharacterGridMetrics.ColumnsPerRow] 张干员卡」反推，
 * 高度沿用原来的 2:1 高宽比，两者都不超过屏幕可用区域。
 */
@Composable
private fun rememberAdaptiveWindowState(): WindowState {
    val workArea = remember { desktopWorkArea() }
    val contentWidth = CharacterGridMetrics.defaultWindowWidth(platformUiScale())
    val contentHeight = contentWidth * 2
    return rememberWindowState(
        width = workArea?.let { contentWidth.coerceAtMost(it.width) } ?: contentWidth,
        height = workArea?.let { contentHeight.coerceAtMost(it.height) } ?: contentHeight,
    ).apply { println(size) }
}

/**
 * 主屏可用区域（已排除任务栏），换算成 Compose 的 dp；取不到时返回 null。
 * 只取主屏；多屏时窗口若被移到其它屏，这个上限只是近似值。
 */
private fun desktopWorkArea(): DpSize? = runCatching {
    val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val scale = environment.defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX
    val bounds = environment.maximumWindowBounds
    if (scale <= 0f || bounds.width <= 0 || bounds.height <= 0) {
        null
    } else {
        DpSize(bounds.width.dp, (bounds.height * 0.8).dp)
    }
}.getOrNull()
