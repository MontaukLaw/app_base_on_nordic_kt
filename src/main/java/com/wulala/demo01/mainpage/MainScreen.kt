package com.wulala.demo01.mainpage

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wulala.demo01.routes.Routes
import com.wulala.demo01.scanner.SettingsScreen
import com.wulala.demo01.screens.HeatmapScreen
import com.wulala.demo01.screens.RealtimeScreen

@Composable
fun MainScreen() {
    val mainNav = rememberNavController()
    val backStack by mainNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomItem("Realtime", Routes.REALTIME, currentRoute, mainNav)
                BottomItem("Heatmap", Routes.HEATMAP, currentRoute, mainNav)
                BottomItem("Settings", Routes.SETTINGS, currentRoute, mainNav)
            }
        }
    ) { padding ->
        NavHost(
            navController = mainNav,
            startDestination = Routes.REALTIME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.REALTIME) { RealtimeScreen() }
            composable(Routes.HEATMAP) { HeatmapScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}

@Composable
fun RowScope.BottomItem(
    label: String,
    route: String,
    currentRoute: String?,
    nav: NavController
) {
    NavigationBarItem(
        selected = currentRoute == route,
        onClick = {
            nav.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(nav.graph.startDestinationId) {
                    saveState = true
                }
            }
        },
        icon = { /* Icon */ },
        label = { Text(label) }
    )
}
