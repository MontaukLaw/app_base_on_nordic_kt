package com.wulala.demo01.routes

import androidx.navigation.NavController

object Routes {
    const val SCANNER = "scanner"
    const val MAIN = "main"
    const val MAIN_WITH_ID = "main/{id}"
}

fun NavController.navigateToMain(id: String) {
    navigate("main/$id") {
        // 防止用户返回又回到“已连接的 Scanner”
        popUpTo(Routes.SCANNER) { inclusive = true }
        launchSingleTop = true
    }
}
