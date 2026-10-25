package com.rainccup.aktool.app

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.rainccup.aktool.core.domain.CharacterStore
import com.rainccup.aktool.core.navigation.AppRoute
import com.rainccup.aktool.feature.character.ui.CharacterPage
import com.rainccup.aktool.feature.characterdetail.ui.CharacterDetailPage
import com.rainccup.aktool.feature.extra.ui.ExtraPage
import com.rainccup.aktool.feature.home.ui.HomePage
import com.rainccup.aktool.feature.setting.ui.SettingPage

/**
 * 路由表：把每个 [AppRoute] 映射到它对应的页面。
 *
 * 返回类型写全 `(AppRoute) -> NavEntry<AppRoute>`，这样 T 在构建时就钉成 AppRoute——
 * 否则入口里 `entry<AppRoute.X>` 的接收者会退化成 NavKey。
 */
internal fun appEntryProvider(
    backStack: NavBackStack<NavKey>,
    characterStore: CharacterStore,
): (AppRoute) -> NavEntry<AppRoute> = entryProvider {
    entry<AppRoute.Home> {
        HomePage()
    }

    entry<AppRoute.Character> {
        CharacterPage { char ->
            backStack.add(AppRoute.CharacterDetail(char))
        }
    }

    entry<AppRoute.Extra> {
        ExtraPage()
    }

    entry<AppRoute.Setting> {
        SettingPage()
    }

    entry<AppRoute.CharacterDetail> {
        CharacterDetailPage(
            character = it.char,
            onSaved = { char ->
                // 唯一数据源就地更新：列表页会据此自动重算可见列表。
                char?.let(characterStore::upsert)
                backStack.removeLastOrNull()
            }
        )
    }
}