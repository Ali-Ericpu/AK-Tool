package com.rainccup.aktool.core.designsystem.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Duration.Companion.milliseconds

/**
 * 一条 toast 的内容。`durationMillis` 跟着消息走，调用方想多停一会儿就自己传。
 */
data class ToastMessage(
    val text: String,
    val durationMillis: Long = LENGTH_SHORT,
) {
    companion object {
        const val LENGTH_SHORT = 2_000L
        const val LENGTH_LONG = 5_000L
    }
}

/**
 * toast 队首（同一时刻只显示一条）。后到的消息直接顶掉前一条——消息总线上的提示多是
 * "这次操作的结果"，排队播报会把过期的结论一路念给用户听。
 */
@Stable
class ToastHostState {

    var current: ToastMessage? by mutableStateOf(null)
        private set

    fun show(text: String, durationMillis: Long = ToastMessage.LENGTH_SHORT) {
        current = ToastMessage(text, durationMillis)
    }

    fun dismiss() {
        current = null
    }

    /**
     * 只关掉自己那一条：定时器比下一次 [show] 晚到时，不至于把新消息一起清掉。
     */
    fun dismiss(message: ToastMessage) {
        if (current === message) current = null
    }
}

@Composable
fun rememberToastHostState(): ToastHostState = remember { ToastHostState() }

/**
 * 想从页面深处弹 toast 时读它：`LocalToast.current?.show(...)`。
 * 宿主没装（比如单独预览某个页面）时为 null，调用方按需降级。
 */
val LocalToast = staticCompositionLocalOf<ToastHostState?> { null }

/**
 * toast 宿主本身：只负责把 [ToastHostState.current] 画出来，位置由调用方的 Modifier 决定
 * （放在页面容器里就贴底居中，放在哪个 Box 里就跟着哪个 Box 走）。
 */
@Composable
fun ToastHost(
    state: ToastHostState,
    modifier: Modifier = Modifier,
) {
    val message = state.current
    LaunchedEffect(message) {
        if (message != null) {
            delay(message.durationMillis.milliseconds)
            state.dismiss(message)
        }
    }
    // 淡出期间 current 已经是 null，得留住最后一条内容，否则气泡会在动画开始前先消失。
    var lastShown by remember { mutableStateOf<ToastMessage?>(null) }
    if (message != null && message !== lastShown) lastShown = message

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn(tween(durationMillis = 140)) +
            scaleIn(tween(durationMillis = 220), initialScale = 0.92f) +
            slideInVertically(tween(durationMillis = 220)) { height -> height / 2 },
        exit = fadeOut(tween(durationMillis = 120)) +
            scaleOut(tween(durationMillis = 120), targetScale = 0.96f),
    ) {
        lastShown?.let { ToastBubble(text = it.text) }
    }
}

@Composable
private fun ToastBubble(text: String) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .imePadding()
            .widthIn(max = 300.dp)
            .shadow(elevation = 12.dp, shape = shape)
            .background(color = MiuixTheme.colorScheme.background, shape = shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}
