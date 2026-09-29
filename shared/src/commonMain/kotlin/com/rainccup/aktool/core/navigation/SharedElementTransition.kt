package com.rainccup.aktool.core.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * [SharedTransitionScope] 的传递通道：由 `AppNavHost` 在 `SharedTransitionLayout` 内提供。
 *
 * 用 CompositionLocal 而不是逐层传参，是因为干员卡被列表页与详情页共用，
 * 传参会把这两个 feature 的页面签名、以及卡片本身都染上转场参数。
 *
 * 不在 `SharedTransitionLayout` 内时为 null（例如单独预览某个页面），
 * 此时 [characterCardSharedElement] 退化成空操作，页面仍能独立渲染。
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * 共享元素的键：同一张干员卡在列表页和详情页必须给出同一个键才能配上对。
 *
 * 用 instId 而不是 charId——同一 charId 可能对应多个实例，只有 instId 唯一。
 */
private fun characterCardKey(instId: Int): String = "character-card-$instId"

/**
 * 干员卡的共享元素修饰符：进入详情页时，卡片从列表页网格中的原位置移动到详情页左上角。
 *
 * 两页的卡片尺寸都是 108×228dp，所以整个转场只需要位移、不需要缩放。
 * 转场期间卡片由 [SharedTransitionScope] 的 overlay 绘制，因此不会被子页面的边界裁掉。
 *
 * 注意 [LocalNavAnimatedContentScope] 只在 NavEntry 内部有值，本修饰符必须在 `NavDisplay`
 * 的 entry 内容里使用。
 *
 * 没有 [LocalSharedTransitionScope] 时原样返回 [this]，保证页面可脱离导航单独渲染。
 */
@Composable
fun Modifier.characterCardSharedElement(instId: Int): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalNavAnimatedContentScope.current
    val base = this
    return with(sharedTransitionScope) {
        base.sharedElement(
            sharedContentState = rememberSharedContentState(key = characterCardKey(instId)),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
