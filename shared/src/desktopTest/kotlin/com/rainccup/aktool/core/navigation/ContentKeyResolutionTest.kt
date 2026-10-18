package com.rainccup.aktool.core.navigation

import com.rainccup.aktool.core.model.Character
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 用 Navigation3 **自己的** `defaultContentKey` 产出的真实 contentKey 来验证还原逻辑。
 *
 * 这条测试的价值：它不写死 contentKey 的字符串格式（实测为
 * `Home:class com.rainccup.aktool.core.navigation.AppRoute$Home`）。库哪天改了格式，
 * 这条测试会先红，而不是让所有 tab 切换悄悄退化成同一个方向。
 *
 * `defaultContentKey` 在 Kotlin 侧不可见，只能反射调用：它是内部实现，但正是
 * `SinglePaneScene.key`（也就是 `Scene.key`）的来源。
 */
class ContentKeyResolutionTest {

    private val defaultContentKey = Class.forName("androidx.navigation3.runtime.NavEntryKt")
        .getMethod("defaultContentKey", Any::class.java)

    @Test
    fun realContentKeysResolveBackToTheirRoutes() {
        NavigationMotion.tabs.forEach { route ->
            val contentKey = defaultContentKey.invoke(null, route)
            assertEquals(
                route,
                NavigationMotion.routeOf(contentKey),
                "the real contentKey for $route must resolve back to it",
            )
        }
    }

    @Test
    fun theDetailPageIsNotMappedToOneOfTheTabs() {
        val detail = AppRoute.CharacterDetail(Character.placeholder())
        assertNull(
            NavigationMotion.routeOf(defaultContentKey.invoke(null, detail)),
            "a detail page must stay outside the tab bar",
        )
    }
}
