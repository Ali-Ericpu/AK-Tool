package com.rainccup.aktool.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene
import com.rainccup.aktool.core.navigation.AppRoute
import com.rainccup.aktool.core.navigation.NavigationMotion
import com.rainccup.aktool.core.navigation.SlideDirection

/* --------------------------------------------------------------------------------------------
 * 页面切换动画
 *
 * 统一的位移方向规则：**新页从目的地在标签栏里所在的那一侧进来**。
 *  - 同级切换：主页→干员 向左推（干员在右边），干员→主页 向右推。方向与标签栏排列一致，
 *    推的幅度只有 1/3 屏，读起来是"横移"而不是"进入下一层"。
 *  - 层级切换：tab ⇄ 详情页，按 tab 的位置决定左右；详情页一律当作"进入下一层"，
 *    进入时新页满屏推入、旧页让位 1/4，返回时镜像。
 *
 * 时长与缓动取自 M3 motion：300ms + FastOutSlowInEasing，淡入与位移共用同一时长，
 * 新旧页同步让位，不会出现一个已经停住另一个还在动。
 * ------------------------------------------------------------------------------------------ */

private val navSpec = NavigationMotion.fadeSpec
private val navSlideSpec = NavigationMotion.slideSpec

/*
 *   slideInHorizontally(initialOffsetX = { +it })  内容自右侧 +W 处出发 → 从右边进入
 *   slideInHorizontally(initialOffsetX = { -it })  内容自左侧 -W 处出发 → 从左边进入
 * 进入与退出必须同向流动：从右进 = 旧页向左出（targetOffsetX 为负）。
 * 因此「目的地更靠右」= 新页从右进 = 旧页左出，两者符号相反。
 */
private val tabSwitchToRight: ContentTransform =
    slideInHorizontally(animationSpec = navSlideSpec, initialOffsetX = { it / 3 }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutHorizontally(animationSpec = navSlideSpec, targetOffsetX = { -it / 3 }) +
            fadeOut(animationSpec = navSpec)

private val tabSwitchToLeft: ContentTransform =
    slideInHorizontally(animationSpec = navSlideSpec, initialOffsetX = { -it / 3 }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutHorizontally(animationSpec = navSlideSpec, targetOffsetX = { it / 3 }) +
            fadeOut(animationSpec = navSpec)

/*
 * 宽屏改用左侧导航栏后 tab 是竖排的，tab 之间的位移也要跟着改成上下方向——
 * 否则"往下切一个 tab"却看到内容横向掠过，方向感是错的。
 * 命名与横向那对保持一致：FromBottom = 新页自下方进入。
 */
private val tabSwitchFromBottom: ContentTransform =
    slideInVertically(animationSpec = navSlideSpec, initialOffsetY = { it / 3 }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutVertically(animationSpec = navSlideSpec, targetOffsetY = { -it / 3 }) +
            fadeOut(animationSpec = navSpec)

private val tabSwitchFromTop: ContentTransform =
    slideInVertically(animationSpec = navSlideSpec, initialOffsetY = { -it / 3 }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutVertically(animationSpec = navSlideSpec, targetOffsetY = { it / 3 }) +
            fadeOut(animationSpec = navSpec)

/* 层级进入：新页满屏推入，被覆盖页只让位 1/4，做出纵深。 */
private val pushToRight: ContentTransform =
    slideInHorizontally(animationSpec = navSlideSpec, initialOffsetX = { it }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutHorizontally(animationSpec = navSlideSpec, targetOffsetX = { -it / 4 }) +
            fadeOut(animationSpec = navSpec)

private val pushToLeft: ContentTransform =
    slideInHorizontally(animationSpec = navSlideSpec, initialOffsetX = { -it }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutHorizontally(animationSpec = navSlideSpec, targetOffsetX = { it / 4 }) +
            fadeOut(animationSpec = navSpec)

/* 层级返回：进入动画的镜像——被返回的页从它"来时的方向"退回去。 */
private val popToLeft: ContentTransform =
    slideInHorizontally(animationSpec = navSlideSpec, initialOffsetX = { -it / 4 }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutHorizontally(animationSpec = navSlideSpec, targetOffsetX = { it }) +
            fadeOut(animationSpec = navSpec)

private val popToRight: ContentTransform =
    slideInHorizontally(animationSpec = navSlideSpec, initialOffsetX = { it / 4 }) +
            fadeIn(animationSpec = navSpec) togetherWith
            slideOutHorizontally(animationSpec = navSlideSpec, targetOffsetX = { -it }) +
            fadeOut(animationSpec = navSpec)

/*
 * Direction.Left 的含义是"内容整体左移"，也就是目的地更靠右、新页从右边进来。
 * 所以 Left 用 ToRight（新页自右侧进入），Right 用 ToLeft。命名与映射都写成
 * 显式语义，避免再出现"名字叫 Left 实际却是从右进"这种自相矛盾。
 */

/**
 * tab 之间的转场。[vertical] 由当前布局决定：底部标签栏是横排的，用横向滑动；
 * 左侧导航栏里 tab 自上而下排列，[SlideDirection.Left]（目的地在标签栏里更靠后）
 * 对应"往下走"，所以新页自下方进入。
 */
private fun tabSwitchTransform(direction: SlideDirection, vertical: Boolean): ContentTransform =
    if (vertical) {
        when (direction) {
            SlideDirection.Left -> tabSwitchFromBottom
            SlideDirection.Right -> tabSwitchFromTop
        }
    } else {
        when (direction) {
            SlideDirection.Left -> tabSwitchToRight
            SlideDirection.Right -> tabSwitchToLeft
        }
    }

private fun pushTransform(direction: SlideDirection): ContentTransform =
    when (direction) {
        SlideDirection.Left -> pushToRight
        SlideDirection.Right -> pushToLeft
    }

private fun popTransform(direction: SlideDirection): ContentTransform =
    when (direction) {
        SlideDirection.Left -> popToLeft
        SlideDirection.Right -> popToRight
    }

/**
 * 目的地对应的标签栏序号；非 tab（详情页等）返回 -1。
 *
 * `NavEntry.key` 是 private，所以读公开的 `contentKey`；再退回 `Scene.key`。
 * 两端都交给 [NavigationMotion.routeOf] 还原（它同时处理路由对象、旧式 Pair 与
 * 形如 `Home:class …AppRoute$Home` 的字符串）。
 */
private fun Scene<AppRoute>.tabIndexOfDestination(): Int {
    val destination = entries.lastOrNull()?.let { NavigationMotion.routeOf(it.contentKey) }
        ?: NavigationMotion.routeOf(key)
    return NavigationMotion.tabIndexOf(destination)
}

/**
 * 进入：tab 之间按标签栏顺序位移（底部标签栏横向 / 左侧导航栏竖向），
 * 下钻详情页按 tab 位置推入。
 *
 * 方向用**标签栏序号**判定（见 [NavigationMotion.tabIndexOf]）。
 * 接收者显式写成 `AnimatedContentTransitionScope<Scene<AppRoute>>`：它同时把
 * NavDisplay 的 T 钉成 AppRoute，否则 T 会被推成 NavKey、接收者退化成 Scene<*>。
 */
internal fun AnimatedContentTransitionScope<Scene<AppRoute>>.navTransitionSpec(
    vertical: Boolean,
): ContentTransform {
    val fromIndex = initialState.tabIndexOfDestination()
    val toIndex = targetState.tabIndexOfDestination()
    val direction = NavigationMotion.direction(fromIndex, toIndex)
    return if (NavigationMotion.sameLevel(fromIndex, toIndex)) {
        tabSwitchTransform(direction, vertical)
    } else {
        // 详情页是"在同一层里进去一页"，与标签栏横排还是竖排无关，始终水平推入
        pushTransform(direction)
    }
}

/**
 * 返回：镜像进入动画。
 *
 * 从详情页退回 tab 时，方向要取"当前是从哪个 tab 进来的"，所以比较的是
 * initialState（详情页的来源）与 targetState（要回去的 tab）。
 * 若反着算，末尾两个 tab 之间退回会与标签栏顺序相反。该不变量由
 * `NavigationMotionTest` 钉住。
 */
internal fun AnimatedContentTransitionScope<Scene<AppRoute>>.navPopTransitionSpec(
    vertical: Boolean,
): ContentTransform {
    val fromIndex = initialState.tabIndexOfDestination()
    val toIndex = targetState.tabIndexOfDestination()
    val entering = NavigationMotion.direction(fromIndex, toIndex)
    return if (NavigationMotion.sameLevel(fromIndex, toIndex)) {
        tabSwitchTransform(entering, vertical)
    } else {
        popTransform(NavigationMotion.exitDirection(entering))
    }
}

/*
 * 预测性返回跟手：位移随手势进度线性推进，因此不加缓动。
 * 方向与真实返回保持一致，手势取消时不会先往一边再弹回另一边。
 * 手势边缘（参数）在当前实现里不参与判断，故不再接这个参数。
 */
internal fun AnimatedContentTransitionScope<Scene<AppRoute>>.navPredictivePopSpec(
    vertical: Boolean,
): ContentTransform {
    val fromIndex = initialState.tabIndexOfDestination()
    val toIndex = targetState.tabIndexOfDestination()
    return if (NavigationMotion.sameLevel(fromIndex, toIndex)) {
        tabSwitchTransform(NavigationMotion.direction(fromIndex, toIndex), vertical)
    } else {
        when (NavigationMotion.direction(toIndex, fromIndex)) {
            SlideDirection.Left ->
                slideInHorizontally(
                    animationSpec = navSlideSpec,
                    initialOffsetX = { -it / 4 },
                ) togetherWith slideOutHorizontally(
                    animationSpec = navSlideSpec,
                    targetOffsetX = { it },
                )

            SlideDirection.Right ->
                slideInHorizontally(
                    animationSpec = navSlideSpec,
                    initialOffsetX = { it / 4 },
                ) togetherWith slideOutHorizontally(
                    animationSpec = navSlideSpec,
                    targetOffsetX = { -it },
                )
        }
    }
}