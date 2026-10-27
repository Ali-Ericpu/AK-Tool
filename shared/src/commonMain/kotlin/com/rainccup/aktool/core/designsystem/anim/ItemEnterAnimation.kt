package com.rainccup.aktool.core.designsystem.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val StaggerStepMillis = 30
private const val StaggerMaxIndex = 10
private const val EnterMillis = 260

/** 第 [index] 项该等多久再开始入场：阶梯只递增到 [StaggerMaxIndex]，此后所有条目同时入场。 */
internal fun itemEnterDelayMillis(index: Int): Long =
    index.coerceIn(0, StaggerMaxIndex) * StaggerStepMillis.toLong()

/**
 * 懒加载列表的入场动画：条目第一次出现时淡入并上浮一小段，按 [index] 阶梯延迟。
 *
 * 用 [rememberSaveable] 记住“已经出现过”，所以往回滚不会重播；列表内容被换掉
 * （切职业、换数据源）时条目会重新组合，动画自然再来一次。
 * 阶梯只按前 [StaggerMaxIndex] 项递增，滚到很后面时不会为了等延迟而空着。
 */
@Composable
fun Modifier.itemEnterAnimation(
    index: Int,
    rise: Dp = 14.dp,
): Modifier {
    val risePx = with(LocalDensity.current) { rise.toPx() }
    var appeared by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (appeared) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (appeared) return@LaunchedEffect
        delay(itemEnterDelayMillis(index))
        appeared = true
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = EnterMillis, easing = FastOutSlowInEasing),
        )
    }

    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * risePx
    }
}
