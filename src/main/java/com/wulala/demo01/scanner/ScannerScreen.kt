package com.wulala.demo01.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wulala.demo01.common.DeviceList
import com.wulala.demo01.common.ScanDevice
import com.wulala.demo01.data.BlePermissionState
import kotlinx.coroutines.flow.collectLatest
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

fun hasBlePermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

const val OUR_DEVICE_NAME = "SYC"

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ScannerScreen(
    onConnected: () -> Unit, vm: ScannerViewModel = hiltViewModel()
) {
    val scanResults by vm.scanResults.collectAsStateWithLifecycle()
    val isScanning by vm.isScanning.collectAsStateWithLifecycle()
    val selectedPeripheral by vm.peripheral.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val permissions = remember { requiredBlePermissions() }

    var navigated by rememberSaveable { mutableStateOf(false) }

    val connectionState by vm.connectionState.collectAsStateWithLifecycle()

    // 记录权限是否已授予（用系统查询作为“真相源”）
    var hasPermission by remember {
        mutableStateOf(hasBlePermissions(context, permissions))
    }

    // 权限 OK 就自动扫（也可只在第一次扫，看你要不要“从设置回来自动扫”）
    LaunchedEffect(hasPermission) {
        if (hasPermission) vm.startScan(nameContains = OUR_DEVICE_NAME)
    }

    // Connected -> 导航一次
    LaunchedEffect(connectionState) {
        if (!navigated && connectionState is ConnectionState.Connected) {
            navigated = true
            onConnected()
        }
    }

    // 发起新连接时重置 gate
    LaunchedEffect(selectedPeripheral?.identifier) {
        navigated = false
    }

    var permissionState by remember { mutableStateOf<BlePermissionState>(BlePermissionState.Denied) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->

        // 1) 找出被拒绝的权限
        val denied = permissions.filter { result[it] != true }

        // 2) 判断“永久拒绝”（只对被拒绝的那几个权限判断）
        val permanentlyDenied = activity != null && denied.any { perm ->
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
        }

        // 3) 更新你的 permissionState
        permissionState = when {
            denied.isEmpty() -> BlePermissionState.Granted
            permanentlyDenied -> BlePermissionState.PermanentlyDenied
            else -> BlePermissionState.Denied
        }

        // 4) 如果已授权，就开始扫描
        if (permissionState == BlePermissionState.Granted) {
            vm.startScan(nameContains = OUR_DEVICE_NAME             )
        }
    }

    // 页面离开：停止扫描
    DisposableEffect(Unit) {
        onDispose { vm.stopScan() }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(vertical = 64.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScannerView(
            devices = scanResults, isScanning = isScanning, onStartScan = {
                // 点击按钮：如果没权限 -> 请求；有权限 -> start/stop toggle
                if (!hasBlePermissions(context, permissions)) {
                    // activity 为空时，不要调用 shouldShow...；直接 launch 也可以
                    permissionLauncher.launch(permissions)
                } else {
                    if (!isScanning) vm.startScan(OUR_DEVICE_NAME) else vm.stopScan()
                }
            }, onPeripheralClicked = vm::connect
        )
    }
}

@Composable
fun ScannerView(
    devices: List<ScanDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onPeripheralClicked: (Peripheral) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onStartScan,
                enabled = true,//!isScanning,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = if (isScanning) "Stop scan" else "Start scan")
            }

            AnimatedVisibility(visible = isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }

        if (devices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Tap on a device to connect.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        DeviceList(
            modifier = Modifier.fillMaxSize(),
            devices = devices,
            onItemClick = onPeripheralClicked,
            contentPadding = PaddingValues(bottom = 56.dp, top = 16.dp),
        )
    }
}

private fun requiredBlePermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}