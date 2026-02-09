package com.wulala.demo01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wulala.demo01.mainpage.MainScreen
import com.wulala.demo01.routes.Routes
import com.wulala.demo01.scanner.ScannerScreen
import com.wulala.demo01.ui.theme.KotlinBLELibraryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinBLELibraryTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.SCAN
                ) {

                    // 1️⃣ 扫描页（入口）
                    composable(Routes.SCAN) {
                        ScannerScreen(
                            onConnected = {
                                navController.navigate(Routes.MAIN) {
                                    // 🔥 关键：把 Scan 从返回栈里清掉
                                    popUpTo(Routes.SCAN) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    // 2️⃣ 主页面（BottomBar）
                    composable(Routes.MAIN) {
                        MainScreen()
                    }
                }
            }
        }
    }
}