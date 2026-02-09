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
import androidx.navigation.compose.rememberNavController
import com.wulala.demo01.mainpage.MainScreen
import com.wulala.demo01.scanner.ScannerScreen
import com.wulala.demo01.ui.theme.KotlinBLELibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinBLELibraryTheme {
                val rootNav = rememberNavController()

                NavHost(
                    navController = rootNav,
                    startDestination = Routes.SCAN
                ) {
                    composable(Routes.SCAN) {
                        ScannerScreen(
                            onConnected = {
                                rootNav.navigate(Routes.MAIN) {
                                    popUpTo(Routes.SCAN) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.MAIN) {
                        MainScreen()
                    }
                }
            }
        }
    }
}