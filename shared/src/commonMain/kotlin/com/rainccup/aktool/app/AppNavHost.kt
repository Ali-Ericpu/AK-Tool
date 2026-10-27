package com.rainccup.aktool.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.designsystem.LargeScreenBreakpoint
import com.rainccup.aktool.core.designsystem.LocalFloatingBarInset
import com.rainccup.aktool.core.designsystem.LocalLargeScreen
import com.rainccup.aktool.core.designsystem.floatingBarMetrics
import com.rainccup.aktool.core.designsystem.toast.LocalToast
import com.rainccup.aktool.core.designsystem.toast.ToastHost
import com.rainccup.aktool.core.designsystem.toast.rememberToastHostState
import com.rainccup.aktool.core.domain.CharacterStore
import com.rainccup.aktool.core.message.MessageBus
import com.rainccup.aktool.core.navigation.AppRoute
import com.rainccup.aktool.core.navigation.LocalSharedTransitionScope
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
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 返回栈里可能出现哪些路由：多态序列化必须显式登记，否则进程重建后反序列化不认识。 */
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
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

/**
 * 应用骨架：底部（或宽屏左侧）标签栏 + 页面容器。
 *
 * 分工：
 *  - [appTabs] / [AppBottomBar] / [AppNavigationRail]（AppNavChrome.kt）——标签栏长什么样；
 *  - [appEntryProvider]（AppNavEntries.kt）——每个路由对应哪个页面；
 *  - [navTransitionSpec] 等（AppNavTransitions.kt）——页面之间怎么动；
 *  - 本文件只负责把这几块接起来，以及决定"内容能不能穿到底栏下面"。
 *
 * @param background 自定义背景，必须落在 backdrop 层内，玻璃底栏才采样得到它。
 */
@Composable
fun AppNavHost(background: @Composable () -> Unit = {}) {
    val backStack = rememberNavBackStack(navConfig, AppRoute.Home)
    val characterStore: CharacterStore = koinInject()

    val homeLabel = stringResource(Res.string.home)
    val characterLabel = stringResource(Res.string.character)
    val extraLabel = stringResource(Res.string.extra)
    val settingLabel = stringResource(Res.string.setting)
    val tabs = remember(homeLabel, characterLabel, extraLabel, settingLabel) {
        appTabs(homeLabel, characterLabel, extraLabel, settingLabel)
    }

    val currentTop = backStack.lastOrNull()
    // 切 Tab：只在目标不是当前页时入栈
    val onTabSelect: (AppRoute) -> Unit = { route ->
        if (currentTop != route) {
            if (route is AppRoute.Character && currentTop is AppRoute.CharacterDetail) {
                backStack.removeLastOrNull()
            } else {
                backStack.add(route)
            }
        }
    }

    val messageBus: MessageBus = koinInject()
    // 玻璃底栏的背景来源：图层里同时装自定义背景与页面内容，
    // 底栏再用 textureBlur 在同一形状里绘制模糊后的它。
    val surface = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surface)
        drawContent()
    }
    // 底栏是浮层：内容底部不留白，一路铺到它下面穿过去（玻璃要采样到滑过去的内容才有质感），
    // 被它压住的那一段通过 LocalFloatingBarInset 交给各页面的滚动容器。
    val toast = rememberToastHostState()
    LaunchedEffect(messageBus) {
        messageBus.messages.collectLatest {
            Logger.i(tag = "LOG_MESSAGE") { it }

            toast.show(it)
        }
    }

    // 大屏（桌面 / 平板横屏）改用左侧导航栏；窄屏仍保持底部标签栏，两种布局共用同一份 tabs 与选中逻辑。
    BoxWithConstraints {
        val useNavigationRail = maxWidth >= LargeScreenBreakpoint
        val bottomBarVisible = !useNavigationRail && tabs.any { currentTop == it.route }
        // 底栏是浮层、高度随文字缩放变化，toast 得知道它实际占多少才能浮在它上方。
        var barReserve by remember { mutableStateOf(0.dp) }
        // 底栏放进 bottomBar 槽而不是外层的 Box：miuix 把弹窗画在 Scaffold 的 popup 层里（最后绘制），
        // 底栏待在槽里就处在这层之下，弹窗仍然压得住底栏；挂到 Scaffold 外面反而会盖住弹窗。
        // 顺带的好处：paddingValues.bottom 直接就是底栏的实测高度，不用再自己量 root 坐标。
        CompositionLocalProvider(LocalToast provides toast) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AppBottomBar(
                        visible = bottomBarVisible,
                        tabs = tabs,
                        currentTop = currentTop,
                        backdrop = backdrop,
                        onTabSelect = onTabSelect,
                    )
                },
            ) { paddingValues ->
                // rememberNavBackStack 的静态类型是 SnapshotStateList<NavKey>，但塞进去的始终是 AppRoute。
                // 固定成 List<AppRoute> 才能让 NavDisplay 的 T 定下来：否则 T 被推成 NavKey，
                // 三个 transitionSpec 的接收者就变成 Scene<NavKey>，扩展函数全部失配。
                @Suppress("UNCHECKED_CAST")
                val routes = backStack as List<AppRoute>
                // Scaffold 给的 paddingValues 拆成「内容内边距」与「底栏占位」，细节见 FloatingBar.kt。
                val bar = floatingBarMetrics(paddingValues)
                SideEffect { barReserve = bar.reserve }
                // 背景图必须落在 backdrop 层内，玻璃底栏才采样得到它；
                // 底栏是该层的兄弟节点，若也包进去会把自己录成拖影。
                Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                    background()
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useNavigationRail) {
                            AppNavigationRail(
                                tabs = tabs,
                                currentTop = currentTop,
                                onTabSelect = onTabSelect,
                            )
                        }
                        // 共享元素转场的作用域：干员卡要在列表页与详情页之间连续移动，必须由同一个
                        // SharedTransitionLayout 统一测量两个页面的坐标，并由它负责转场期间的 overlay 绘制
                        // （否则移动中的卡片会被页面的边界裁掉）。
                        SharedTransitionLayout(modifier = Modifier.weight(1f)) {
                            val sharedTransitionScope = this
                            // 大屏判定只在这里做一次：页面通过 LocalLargeScreen 读同一个结果来分栏。
                            CompositionLocalProvider(
                                LocalSharedTransitionScope provides sharedTransitionScope,
                                LocalLargeScreen provides useNavigationRail,
                                LocalFloatingBarInset provides bar.reserve,
                            ) {
                                NavDisplay(
                                    backStack = routes,
                                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                                    sharedTransitionScope = sharedTransitionScope,
                                    modifier = Modifier
                                        // 左右 / 顶部照常吃 Scaffold 的内边距，底部只吃系统栏：内容要一路铺到
                                        // 底栏底下穿过去，模糊才有东西可采样；最后一项的留白交给各页面的
                                        // 滚动容器（LocalFloatingBarInset）。
                                        .padding(bar.contentPadding)
                                        .padding(start = 12.dp, end = 12.dp, top = 12.dp),
                                    transitionSpec = { navTransitionSpec(useNavigationRail) },
                                    popTransitionSpec = { navPopTransitionSpec(useNavigationRail) },
                                    predictivePopTransitionSpec = {
                                        navPredictivePopSpec(useNavigationRail)
                                    },
                                    entryDecorators = listOf(
                                        rememberSaveableStateHolderNavEntryDecorator(),
                                        rememberViewModelStoreNavEntryDecorator(),
                                    ),
                                    entryProvider = appEntryProvider(backStack, characterStore),
                                )
                            }
                        }
                    }
                }
            }
            // toast 画在 Scaffold 之上，自然也在玻璃底栏之上——底栏是浮层，被压住会很难看。
            ToastHost(
                state = toast,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = barReserve + 12.dp),
            )
        }
    }
}