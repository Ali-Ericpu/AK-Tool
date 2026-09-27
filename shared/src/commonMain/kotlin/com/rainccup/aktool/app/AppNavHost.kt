package com.rainccup.aktool.app

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.rainccup.aktool.core.message.MessageBus
import com.rainccup.aktool.core.navigation.AppRoute
import com.rainccup.aktool.feature.character.ui.CharacterPage
import com.rainccup.aktool.feature.characterdetail.ui.CharacterDetailPage
import com.rainccup.aktool.feature.extra.ui.ExtraPage
import com.rainccup.aktool.feature.home.ui.HomePage
import com.rainccup.aktool.feature.setting.ui.SettingPage
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.character
import com.rainccup.aktool.resources.extra
import com.rainccup.aktool.resources.home
import com.rainccup.aktool.resources.setting
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(navConfig, AppRoute.Home)
    val homeLabel = stringResource(Res.string.home)
    val characterLabel = stringResource(Res.string.character)
    val extraLabel = stringResource(Res.string.extra)
    val settingLabel = stringResource(Res.string.setting)
    val tabs = remember(homeLabel, characterLabel, extraLabel, settingLabel) {
        listOf(
            TabMeta(AppRoute.Home, homeLabel, Icons.Default.Home),
            TabMeta(AppRoute.Character, characterLabel, Icons.Default.AccountBox),
            TabMeta(AppRoute.Extra, extraLabel, Icons.Default.Build),
            TabMeta(AppRoute.Setting, settingLabel, Icons.Default.Settings),
        )
    }
    val currentTop = backStack.lastOrNull()

    val messageBus: MessageBus = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(messageBus) {
        messageBus.messages.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(state = snackbarHostState) },
        contentWindowInsets = WindowInsets(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AK TOOL",
                        modifier = Modifier.padding(12.dp)
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTop == tab.route,
                        onClick = {
                            if (currentTop != tab.route) {
                                backStack.add(tab.route)
                            }
                        },
                        icon = tab.icon,
                        label = tab.label,
                    )
                }
            }
        },
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            modifier = Modifier
                .padding(paddingValues)
                .padding(12.dp),
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<AppRoute.Home> {
                    HomePage()
                }

                entry<AppRoute.Character> {
                    CharacterPage(it.char) { char ->
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
                            backStack.removeLastOrNull()
                            val last = backStack.lastOrNull()
                            if (last is AppRoute.Character) {
                                last.char = char
                            }
                        }
                    )
                }
            },
        )
    }
}

