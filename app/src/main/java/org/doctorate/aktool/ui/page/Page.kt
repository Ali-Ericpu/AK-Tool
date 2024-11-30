package org.doctorate.aktool.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.doctorate.aktool.ui.page.character.CharacterPage
import org.doctorate.aktool.ui.page.setting.Setting

enum class Page(val route: String, val desc: String, val icon: ImageVector) {
    HOME("home", "主页", Icons.Default.Home),
    CHARACTER("character", "干员", Icons.Default.AccountBox),
    EXTRA("extra", "更多", Icons.Default.Build),
    SETTING("setting", "设置", Icons.Default.Settings);

    companion object {
        fun getRoute(route: String?): Page {
            if (route == null) return HOME
            return entries.find { it.route == route } ?: HOME
        }
    }
}

@Preview
@Composable
fun RoutePage() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentPage = Page.getRoute(backStackEntry.value?.destination?.route)
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Page.HOME.route,
//                enterTransition = {
//                    fadeIn(
//                        initialAlpha = 0.1f,
//                        animationSpec = tween(400)
//                    )
//                },
//                exitTransition = {
//                    fadeOut(
//                        targetAlpha = 0f,
//                        animationSpec = tween(400)
//                    )
//                },
//                popEnterTransition = {
//                    fadeIn(
//                        initialAlpha = 0.1f,
//                        animationSpec = tween(400)
//                    )
//                },
//                popExitTransition = {
//                    fadeOut(
//                        targetAlpha = 0f,
//                        animationSpec = tween(400)
//                    )
//                },
                modifier = Modifier
                    .weight(9f)
                    .padding(12.dp)
                    .statusBarsPadding()
            ) {
                composable(route = Page.HOME.route) {
                    HomePage()
                }
                composable(route = Page.CHARACTER.route) {
                    CharacterPage()
                }
                composable(route = Page.EXTRA.route) {
                    Text("EXTRA")
                }
                composable(route = Page.SETTING.route) {
                    Setting()
                }
            }
            NavigationBar {
                Page.entries.map {
                    val color =
                        if (it == currentPage) MaterialTheme.colorScheme.primary else Color.Gray
                    NavigationBarItem(
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                imageVector = it.icon,
                                contentDescription = it.name,
                                tint = color,
                            )
                        },
                        label = { Text(text = it.desc, color = color) },
                        selected = it == currentPage,
                        onClick = { navController.navigateSingleTopTo(it.route) },
                        colors = NavigationBarItemColors(
                            selectedIconColor = color,
                            selectedTextColor = color,
                            selectedIndicatorColor = color.copy(alpha = 0F),
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            disabledIconColor = Color.Gray,
                            disabledTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) = navigate(route) {
    popUpTo(
        this@navigateSingleTopTo.graph.findStartDestination().route ?: Page.HOME.route
    ) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
