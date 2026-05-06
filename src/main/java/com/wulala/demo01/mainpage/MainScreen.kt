package com.wulala.demo01.mainpage

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.wulala.demo01.scanner.*
import com.wulala.demo01.screens.matrix.MatrixScreen
import com.wulala.demo01.screens.realtime.RealtimeScreen
import com.wulala.demo01.screens.wave.WaveScreen
import no.nordicsemi.kotlin.ble.core.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onExit: () -> Unit,
    vm: MainViewModel = hiltViewModel()
) {

    val p by vm.peripheral.collectAsStateWithLifecycle()
    val connectionState by vm.connectionState.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    // ✅ 避免初始状态（Disconnected(null)）就把你踢回去
    var everConnected by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            everConnected = true
        }
        if (everConnected && connectionState is ConnectionState.Disconnected) {
            onExit()
        }
    }

    // ✅ 关键：MainScreen 内部自己的 NavController
    val tabNav = rememberNavController()

    val tabs = listOf(
        "wave" to "Wave",
        "matrix" to "Matrix",
        "settings" to "Settings"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text((p?.name ?: p?.identifier ?: "Main") + " FPS: ${uiState.fps}, Bytes per sec: ${uiState.bytesPerSec}") },
                actions = {
                    TextButton(onClick = {
                        vm.disconnect()
                    }) { Text("Disconnect") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val backStackEntry by tabNav.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                tabs.forEach { (route, label) ->
                    NavigationBarItem(
                        selected = (currentRoute == route),
                        onClick = {
                            tabNav.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                // ✅ 关键：避免堆栈越点越深
                                popUpTo(tabNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        },
                        icon = { /* Icon(...) */ },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->

        // ✅ 关键：tabNav 的图里必须声明 wave/matrix/settings
        NavHost(
            navController = tabNav,
            startDestination = "wave",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            composable("wave") { WaveScreen(vm) }
            composable("matrix") { MatrixScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
