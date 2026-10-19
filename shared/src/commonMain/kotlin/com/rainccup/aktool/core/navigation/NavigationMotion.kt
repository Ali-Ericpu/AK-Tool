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

    /**
     * 把"场景里表示目的地的那个值"还原成路由。
     *
     * `SinglePaneScene.key` 取的就是 `NavEntry.contentKey`（见 SinglePaneSceneStrategy 的字节码）。
     * 它必须能塞进 Android 的 `Bundle`（`SaveableStateHolder` 拿它当状态键），所以只能保持
     * Navigation3 默认的字符串形式，形如
     * `Home:class com.rainccup.aktool.core.navigation.AppRoute$Home`。
     *
     * 解析只取 `":class "` **之前**那段，与路由自己的 `toString()` 比较：
     * 两边走的是同一个 `toString()`，所以 R8 改写类名之后仍然相等；
     * 类名部分（包括 Android 上 `" (Kotlin reflection is not available)"` 后缀）完全不参与判定。
     * 详情页的 `toString()` 是 `CharacterDetail(char=…)`，不会与任何 tab 相等，因此被判成"非 tab"。
     */
    fun routeOf(candidate: Any?): AppRoute? = when (candidate) {
        is AppRoute -> candidate
        is Pair<*, *> -> (candidate.first as? AppRoute)
            ?: tabs.firstOrNull { it.toString() == candidate.first?.toString() }

        is String -> {
            val label = candidate.substringBefore(":class ")
            tabs.firstOrNull { it.toString() == label }
        }

        else -> null
    }

    fun isTab(route: AppRoute?): Boolean = route in tabs

    /**
     * 目的地在标签栏中的序号；非 tab（含详情页）返回 -1。
     *
     * 调用方直接传目的地路由本身（来自 `Scene.entries`，类型就是 AppRoute）。
     * 以前这里从 `Scene.key` 里解析 `Pair` 与类名，那是 Navigation3 的内部形状：
     * 库一升级就全部解析失败、序号恒为 -1，表现是"点任何 tab 都往同一个方向动"。
     */
    fun tabIndexOf(route: AppRoute?): Int = tabs.indexOf(route)

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
