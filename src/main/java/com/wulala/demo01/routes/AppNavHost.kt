package com.wulala.demo01.routes
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wulala.demo01.mainpage.MainScreen
import com.wulala.demo01.scanner.ScannerScreen

@Composable
fun AppNavHost() {
    val rootNav = rememberNavController()

    NavHost(
        navController = rootNav,
        startDestination = Routes.SCANNER
    ) {
        composable(Routes.SCANNER) {
            ScannerScreen(
                onConnected = {
                    // ✅ 不带 id，直接去 main
                    rootNav.navigate(Routes.MAIN) {
                        popUpTo(Routes.SCANNER) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onExit = {
                    // ✅ 主动退出 main（断开/失败）回 scanner
                    rootNav.navigate(Routes.SCANNER) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}