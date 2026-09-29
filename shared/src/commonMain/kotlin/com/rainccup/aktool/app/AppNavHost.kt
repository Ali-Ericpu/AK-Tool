package com.rainccup.aktool.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.rainccup.aktool.core.message.MessageBus
import com.rainccup.aktool.core.navigation.AppRoute
import com.rainccup.aktool.core.navigation.LocalSharedTransitionScope
import com.rainccup.aktool.core.navigation.NavigationMotion
import com.rainccup.aktool.core.navigation.SlideDirection
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

private data class TabMeta(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

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
private fun tabSwitchTransform(direction: SlideDirection): ContentTransform =
    when (direction) {
        SlideDirection.Left -> tabSwitchToRight
        SlideDirection.Right -> tabSwitchToLeft
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
 * 进入：tab 之间按标签栏顺序横移，下钻详情页按 tab 位置推入。
 *
 * 方向用**标签栏序号**判定（见 [NavigationMotion.tabIndexOfSceneKey]）；
 * λ 声明成有显式类型的顶层 val 而不是匿名 λ 传参，否则 NavDisplay 的 T
 * 无法从 backStack 定下来，接收者会退化成 Scene<*>。
 */
private val navTransitionSpec:
    AnimatedContentTransitionScope<Scene<AppRoute>>.() -> ContentTransform = {
    val fromIndex = NavigationMotion.tabIndexOfSceneKey(initialState.key)
    val toIndex = NavigationMotion.tabIndexOfSceneKey(targetState.key)
    val direction = NavigationMotion.direction(fromIndex, toIndex)
    if (NavigationMotion.sameLevel(fromIndex, toIndex)) {
        tabSwitchTransform(direction)
    } else {
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
private val navPopTransitionSpec:
    AnimatedContentTransitionScope<Scene<AppRoute>>.() -> ContentTransform = {
    val fromIndex = NavigationMotion.tabIndexOfSceneKey(initialState.key)
    val toIndex = NavigationMotion.tabIndexOfSceneKey(targetState.key)
    val entering = NavigationMotion.direction(fromIndex, toIndex)
    if (NavigationMotion.sameLevel(fromIndex, toIndex)) {
        tabSwitchTransform(entering)
    } else {
        popTransform(NavigationMotion.exitDirection(entering))
    }
}

/**
 * 预测性返回跟手：位移随手势进度线性推进，因此不加缓动。
 * 方向与真实返回保持一致，手势取消时不会先往一边再弹回另一边。
 */
private val navPredictivePopSpec:
    AnimatedContentTransitionScope<Scene<AppRoute>>.(Int) -> ContentTransform = { _ ->
    val fromIndex = NavigationMotion.tabIndexOfSceneKey(initialState.key)
    val toIndex = NavigationMotion.tabIndexOfSceneKey(targetState.key)
    if (NavigationMotion.sameLevel(fromIndex, toIndex)) {
        tabSwitchTransform(NavigationMotion.direction(fromIndex, toIndex))
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
        // rememberNavBackStack 的静态类型是 SnapshotStateList<NavKey>，但塞进去的始终是 AppRoute。
        // 固定成 List<AppRoute> 才能让 NavDisplay 的 T 定下来：否则 T 被推成 NavKey，
        // 下面三个 transitionSpec 的接收者就变成 Scene<NavKey>，扩展函数全部失配。
        @Suppress("UNCHECKED_CAST")
        val routes = backStack as List<AppRoute>
        // 共享元素转场的作用域：干员卡要在列表页与详情页之间连续移动，必须由同一个
        // SharedTransitionLayout 统一测量两个页面的坐标，并由它负责转场期间的 overlay 绘制
        // （否则移动中的卡片会被页面的边界裁掉）。
        SharedTransitionLayout {
            val sharedTransitionScope = this
            CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
                NavDisplay(
                    backStack = routes,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                    sharedTransitionScope = sharedTransitionScope,
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(12.dp),
                    transitionSpec = navTransitionSpec,
                    popTransitionSpec = navPopTransitionSpec,
                    predictivePopTransitionSpec = navPredictivePopSpec,
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
    }
}
