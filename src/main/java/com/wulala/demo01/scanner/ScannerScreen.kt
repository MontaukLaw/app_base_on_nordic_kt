package com.wulala.demo01.scanner

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wulala.demo01.common.DeviceList
import com.wulala.demo01.data.BlePermissionState
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState

fun hasBlePermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun ScannerScreen() {
    val vm = hiltViewModel<ScannerViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    val devices by vm.peripherals.collectAsStateWithLifecycle()
    val isScanning by vm.isScanning.collectAsStateWithLifecycle()

    // 获取当前上下文并将其转换为Activity，以便在需要时请求权限。
    val context = LocalContext.current
    val activity = context as Activity
    val permissions = requiredBlePermissions()

    var permissionState by remember { mutableStateOf<BlePermissionState>(BlePermissionState.Denied) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.values.all { it }
            permissionState = when {
                allGranted -> BlePermissionState.Granted
                permissions.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                } -> BlePermissionState.PermanentlyDenied

                else -> BlePermissionState.Denied
            }

            if (permissionState == BlePermissionState.Granted) {
                vm.onScanRequested()
            }
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Bluetooth state: $state")

        // Both Bluetooth and Location permissions are granted.
        // We can now start scanning.
        ScannerView(
            devices = devices,
            isScanning = isScanning,
            onStartScan = {
                if (hasBlePermissions(context, permissions)) {
                    if (!isScanning) vm.onScanRequested()
                    else vm.onStopScanRequested()
                } else {
                    permissionLauncher.launch(permissions)
                }
            },
            onPeripheralClicked = vm::onPeripheralSelected,
            onBondRequested = vm::onBondRequested,
            onRemoveBondRequested = vm::onRemoveBondRequested,
            onClearCacheRequested = vm::onClearCacheRequested,
        )
    }
}

@Composable
fun ScannerView(
    devices: List<Peripheral>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onPeripheralClicked: (Peripheral) -> Unit,
    onBondRequested: (Peripheral) -> Unit,
    onRemoveBondRequested: (Peripheral) -> Unit,
    onClearCacheRequested: (Peripheral) -> Unit,
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
            onBondRequested = onBondRequested,
            onRemoveBondRequested = onRemoveBondRequested,
            onClearCacheRequested = onClearCacheRequested,
            contentPadding = PaddingValues(bottom = 56.dp, top = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScannerScreenPreview() {
    val scope = rememberCoroutineScope()
    ScannerView(
        devices = listOf(
            PreviewPeripheral(
                scope = scope,
                address = "00:11:22:33:44:55",
                name = "Device 1",
                state = ConnectionState.Connected,
            ),
            PreviewPeripheral(scope, "11:22:33:44:55:66", "Device 2"),
            PreviewPeripheral(scope, "22:33:44:55:66:77", "Device 3"),
        ),
        isScanning = true,
        onStartScan = {},
        onPeripheralClicked = {},
        onBondRequested = {},
        onRemoveBondRequested = {},
        onClearCacheRequested = {},
    )
}

@Composable
private fun requiredBlePermissions(): Array<String> {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        arrayOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}