package com.rainccup.aktool.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * 导航位移方向：新页从目的地在标签栏里所在的那一侧进来。
 *
 * 单独抽出来是为了可测：方向算错（例如返回时把方向算反）在界面上表现为"动画看着别扭"，
 * 很难靠肉眼稳定发现，但用测试可以钉死。见 `NavigationMotionTest`。
 */
enum class SlideDirection { Left, Right }

/** 一次目的地变更该往哪边移动；方向由标签栏排列决定。 */
object NavigationMotion {

    /** 位移与淡入淡出共用的时长（tab 切换与层级推入共用）。 */
    const val DURATION_MS: Int = 300

    /** 标签栏从左到右的排列——切换方向就按这个顺序判定。增删或调整 tab 顺序时要同步改这里。 */
    val tabs: List<AppRoute> = listOf(
        AppRoute.Home,
        AppRoute.Character,
        AppRoute.Extra,
        AppRoute.Setting,
    )

    val easing = FastOutSlowInEasing
    val fadeSpec = tween<Float>(durationMillis = DURATION_MS, easing = easing)
    val slideSpec = tween<IntOffset>(durationMillis = DURATION_MS, easing = easing)

    fun isTab(route: AppRoute?): Boolean = route in tabs

    /** 路由的标签名，需与 `Scene.key` 里 `"${key::class}"` 的末段保持一致。 */
    fun label(route: AppRoute): String = when (route) {
        AppRoute.Home -> "Home"
        AppRoute.Character -> "Character"
        AppRoute.Extra -> "Extra"
        AppRoute.Setting -> "Setting"
        is AppRoute.CharacterDetail -> "CharacterDetail"
    }

    /**
     * 从 `Scene.key` 解析目的地在标签栏中的序号；非 tab（含详情页）返回 -1。
     *
     * `SinglePaneSceneStrategy` 把 `entries.last().contentKey` 直接当作 `Scene.key`，
     * 而 `contentKey` 是 `Pair("$key", "${key::class}")`——**不是** AppRoute 本身。
     * 曾经在这里写成 `scene.key as? AppRoute`，结果恒为 null、方向判定永远是兜底值，
     * 表现为"点任何 tab 都往同一个方向动"。所以只解析类名，不依赖类型转换。
     */
    fun tabIndexOfSceneKey(key: Any?): Int {
        val text = (key as? Pair<*, *>)?.first?.toString() ?: return -1
        val simple = text.substringAfterLast('.').substringAfterLast('$')
        return tabs.indexOfFirst { label(it) == simple }
    }

    /**
     * 一次目的地变更该往哪边移动；方向只由标签栏序号决定。
     *
     * 端点为非 tab（详情页，序号 -1）→ 按"进入下一层"处理，返回 [SlideDirection.Right]。
     */
    fun direction(fromTabIndex: Int, toTabIndex: Int): SlideDirection {
        if (fromTabIndex < 0 || toTabIndex < 0) return SlideDirection.Right
        return if (toTabIndex > fromTabIndex) SlideDirection.Left else SlideDirection.Right
    }

    /** 两个端点是否都是标签栏目的地（即同级切换，而非下钻/返回）。 */
    fun sameLevel(fromTabIndex: Int, toTabIndex: Int): Boolean =
        fromTabIndex >= 0 && toTabIndex >= 0

    /**
     * 按路由对象的等价重载，便于测试与调用方直接表达意图。
     *
     * 目标在标签栏里更靠右 → [SlideDirection.Left]（新页从右边进、内容整体左移）。
     */
    fun direction(from: AppRoute?, to: AppRoute?): SlideDirection =
        direction(tabs.indexOf(from), tabs.indexOf(to))

    /**
     * 返回时的退出方向：进入方向的镜像。
     *
     * 返回要在 `popTransitionSpec` 里调用，并且必须传 (initialState, targetState)
     * 顺序的原参数——即"详情页是从哪个 tab 进来的"。若把两者对调，
     * 末尾两个 tab 之间的退回会与标签栏顺序相反。
     */
    fun exitDirection(entering: SlideDirection): SlideDirection = when (entering) {
        SlideDirection.Left -> SlideDirection.Right
        SlideDirection.Right -> SlideDirection.Left
    }
}
