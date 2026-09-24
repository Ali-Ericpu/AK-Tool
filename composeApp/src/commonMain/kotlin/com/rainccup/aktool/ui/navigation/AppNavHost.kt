package com.rainccup.aktool.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.rainccup.aktool.ui.character.CharacterPage
import com.rainccup.aktool.ui.characterdetail.CharacterDetailPage
import com.rainccup.aktool.ui.extra.ExtraPage
import com.rainccup.aktool.ui.home.HomePage
import com.rainccup.aktool.ui.setting.SettingPage
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(androidx.navigation3.runtime.NavKey::class) {
            subclass(AppRoute.Home::class, AppRoute.Home.serializer())
            subclass(AppRoute.Character::class, AppRoute.Character.serializer())
            subclass(AppRoute.Extra::class, AppRoute.Extra.serializer())
            subclass(AppRoute.Setting::class, AppRoute.Setting.serializer())
            subclass(
                AppRoute.CharacterDetail::class,
                AppRoute.CharacterDetail.serializer(),
            )
        }
    }
}

private data class TabMeta(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(navConfig, AppRoute.Home)
    val tabs = remember {
        listOf(
            TabMeta(AppRoute.Home, "涓婚〉", Icons.Default.Home),
            TabMeta(AppRoute.Character, "骞插憳", Icons.Default.AccountBox),
            TabMeta(AppRoute.Extra, "鏇村", Icons.Default.Build),
            TabMeta(AppRoute.Setting, "璁剧疆", Icons.Default.Settings),
        )
    }
    val currentTop = backStack.lastOrNull()

    Column(Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            modifier = Modifier
                .weight(9f)
                .padding(12.dp)
                .statusBarsPadding(),
            entryProvider = { key ->
                when (key) {
                    AppRoute.Home -> NavEntry(key) { HomePage() }
                    AppRoute.Character -> NavEntry(key) {
                        CharacterPage(onOpenDetail = { id ->
                            backStack.add(AppRoute.CharacterDetail(id))
                        })
                    }
                    AppRoute.Extra -> NavEntry(key) { ExtraPage() }
                    AppRoute.Setting -> NavEntry(key) { SettingPage() }
                    is AppRoute.CharacterDetail -> NavEntry(key) {
                        CharacterDetailPage(charInstId = key.charInstId, onSaved = { backStack.removeLastOrNull() })
                    }
                    else -> NavEntry(key) { Text("Unknown") }
                }
            },
        )
        NavigationBar(Modifier.alpha(0.9f)) {
            tabs.forEach { tab ->
                val selected = currentTop == tab.route
                val color =
                    if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentTop != tab.route) {
                            backStack.clear()
                            backStack.add(tab.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = color,
                        )
                    },
                    label = { Text(tab.label, color = color) },
                    colors = NavigationBarItemColors(
                        selectedIconColor = color,
                        selectedTextColor = color,
                        selectedIndicatorColor = color.copy(alpha = 0f),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        disabledIconColor = Color.Gray,
                        disabledTextColor = Color.Gray,
                    ),
                )
            }
        }
    }
}

