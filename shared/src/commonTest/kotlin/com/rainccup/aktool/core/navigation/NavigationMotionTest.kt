package com.rainccup.aktool.core.navigation

import com.rainccup.aktool.core.model.Character
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 导航位移方向的回归测试。
 *
 * 这些断言针对的是"看着别扭但不会崩"的那类缺陷：方向算错在运行时没有任何报错，
 * 只有肉眼能察觉。上一次 tab 切换被做成无方向淡入淡出，以及返回方向容易算反，
 * 都属于这种。
 */
class NavigationMotionTest {

    private val detail = AppRoute.CharacterDetail(Character.placeholder())

    @Test
    fun tabOrderIsLeftToRightAsShownInTheBottomBar() {
        assertEquals(
            listOf(AppRoute.Home, AppRoute.Character, AppRoute.Extra, AppRoute.Setting),
            NavigationMotion.tabs,
        )
    }

    @Test
    fun movingToATabOnTheRightSlidesLeft() {
        // 主页 -> 干员：干员在右边，内容整体左移，新页从右侧进入。
        assertEquals(
            SlideDirection.Left,
            NavigationMotion.direction(AppRoute.Home, AppRoute.Character),
        )
        assertEquals(
            SlideDirection.Left,
            NavigationMotion.direction(AppRoute.Extra, AppRoute.Setting),
        )
    }

    @Test
    fun movingToATabOnTheLeftSlidesRight() {
        assertEquals(
            SlideDirection.Right,
            NavigationMotion.direction(AppRoute.Setting, AppRoute.Extra),
        )
        assertEquals(
            SlideDirection.Right,
            NavigationMotion.direction(AppRoute.Character, AppRoute.Home),
        )
    }

    @Test
    fun everyTabPairHasAStableDirection() {
        // 任意两个 tab 之间都必须能判定方向，且反向必然相反。
        NavigationMotion.tabs.forEach { from ->
            NavigationMotion.tabs.forEach { to ->
                if (from == to) return@forEach
                val forward = NavigationMotion.direction(from, to)
                val backward = NavigationMotion.direction(to, from)
                assertTrue(
                    forward != backward,
                    "direction($from, $to) must be the opposite of direction($to, $from)",
                )
            }
        }
    }

    /** 详情页不在标签栏里，方向判定不能崩；两侧都应按"进入下一层"处理。 */
    @Test
    fun detailPageCountsAsDescendingALevel() {
        assertEquals(SlideDirection.Right, NavigationMotion.direction(AppRoute.Character, detail))
        assertEquals(SlideDirection.Right, NavigationMotion.direction(detail, AppRoute.Character))
        assertEquals(SlideDirection.Right, NavigationMotion.direction(null, AppRoute.Home))
        assertEquals(SlideDirection.Right, NavigationMotion.direction(AppRoute.Home, null))
    }

    @Test
    fun exitDirectionMirrorsTheEnteringDirection() {
        assertEquals(SlideDirection.Right, NavigationMotion.exitDirection(SlideDirection.Left))
        assertEquals(SlideDirection.Left, NavigationMotion.exitDirection(SlideDirection.Right))
    }

    /**
     * 详情页一律"从右侧下钻"，与它挂在哪个 tab 下无关——这是 push 的通用约定，
     * 若改成按所属 tab 的位置定方向，从"更多/设置"进入详情页时会变成从左侧推入，反而怪。
     */
    @Test
    fun detailPageAlwaysDescendsFromTheRightWhicheverTabItBelongsTo() {
        NavigationMotion.tabs.forEach { tab ->
            assertEquals(
                SlideDirection.Right,
                NavigationMotion.direction(tab, detail),
                "entering a detail page from $tab must push in from the right",
            )
        }
    }

    /**
     * 这条盯的是上次的坑：返回时方向必须取"进入时"的方向再镜像。
     *
     * 进入详情页恒为 Right，所以返回恒为 Left（详情页右移退出、tab 从左回位）。
     */
    @Test
    fun poppingBackFromADetailPageReversesTheDirectionItWasEnteredWith() {
        NavigationMotion.tabs.forEach { tab ->
            val entering = NavigationMotion.direction(tab, detail)
            assertEquals(SlideDirection.Right, entering)
            assertEquals(
                SlideDirection.Left,
                NavigationMotion.exitDirection(entering),
                "returning from a detail page opened under $tab must mirror the push",
            )
        }
    }

    @Test
    fun tabSwitchingBackIsMirrored() {
        val forward = NavigationMotion.direction(AppRoute.Home, AppRoute.Setting)
        val back = NavigationMotion.direction(AppRoute.Setting, AppRoute.Home)
        assertEquals(SlideDirection.Left, forward)
        assertEquals(SlideDirection.Right, back)
    }

    @Test
    fun isTabRecognisesExactlyTheFourDestinations() {
        NavigationMotion.tabs.forEach { assertTrue(NavigationMotion.isTab(it)) }
        assertTrue(!NavigationMotion.isTab(detail))
        assertTrue(!NavigationMotion.isTab(null))
    }

    /**
     * 回归测试：方向判定曾经恒为兜底值。
     *
     * `SinglePaneSceneStrategy` 把 `NavEntry.contentKey` 当作 `Scene.key`，而它是
     * `Pair("$key", "${key::class}")`，**不是** AppRoute。原先写成 `scene.key as? AppRoute`
     * 于是恒为 null、`direction` 恒返回兜底值 Right——表现是"点任何 tab 都朝同一个方向动"，
     * 且不会有任何报错。
     */
    @Test
    fun sceneKeyResolvesToTabIndex() {
        NavigationMotion.tabs.forEachIndexed { index, route ->
            val key = Pair("$route", "class com.rainccup.aktool.core.navigation.AppRoute\$${
                NavigationMotion.label(route)
            }")
            assertEquals(
                index,
                NavigationMotion.tabIndexOfSceneKey(key),
                "key for $route must resolve to tab index $index",
            )
        }
    }

    @Test
    fun nonTabSceneKeyResolvesToMinusOne() {
        // 详情页在标签栏里没有位置，必须回到兜底分支而不是被误判成某个 tab。
        val detailKey = Pair(
            "CharacterDetail(char=...)",
            "class com.rainccup.aktool.core.navigation.AppRoute\$CharacterDetail",
        )
        assertEquals(-1, NavigationMotion.tabIndexOfSceneKey(detailKey))
        assertEquals(-1, NavigationMotion.tabIndexOfSceneKey(null))
        assertEquals(-1, NavigationMotion.tabIndexOfSceneKey("not-a-pair"))
        assertEquals(-1, NavigationMotion.tabIndexOfSceneKey(Pair("nonsense", "x")))
    }

    @Test
    fun tabSwitchIsSameLevelOnlyWhenBothEndsAreTabs() {
        assertTrue(NavigationMotion.sameLevel(0, 3))
        assertTrue(NavigationMotion.sameLevel(2, 1))
        // 详情页（-1）参与时不是同级切换，应走下钻/返回动画。
        assertTrue(!NavigationMotion.sameLevel(1, -1))
        assertTrue(!NavigationMotion.sameLevel(-1, 1))
        assertTrue(!NavigationMotion.sameLevel(-1, -1))
    }
}
