package com.wulala.demo01.repo

import android.bluetooth.BluetoothDevice
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyInUse
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import kotlin.uuid.ExperimentalUuidApi

@Singleton
class BleRepository @Inject constructor(
    private val centralManager: CentralManager
) {
    // App 级 scope（你之前已经有）
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 当前连接的 Peripheral（连接 / 断开都会更新）
    private val _peripheral = MutableStateFlow<Peripheral?>(null)
    val peripheral: StateFlow<Peripheral?> = _peripheral.asStateFlow()

    /**
     * 连接或断开同一个 peripheral（toggle 行为）
     */
    fun toggleConnection(peripheral: Peripheral) {
        val current = _peripheral.value

        if (current == peripheral) {
            disconnect()
        } else {
            connect(peripheral)
        }
    }

    private fun connect(peripheral: Peripheral) {
        appScope.launch {
            try {
                Timber.i("Connecting to ${peripheral.name}...")
                centralManager.connect(peripheral)
                _peripheral.value = peripheral
                Timber.i("Connected to ${peripheral.name}")
            } catch (e: Exception) {
                Timber.e(e, "Connection failed")
                _peripheral.value = null
            }
        }
    }

    private fun disconnect() {
        appScope.launch {
            val p = _peripheral.value ?: return@launch
            try {
                Timber.i("Disconnecting from ${p.name}...")
                p.disconnect()
            } catch (e: Exception) {
                Timber.e(e, "Disconnect failed")
            } finally {
                _peripheral.value = null
                Timber.i("Disconnected")
            }
        }
    }

    /**
     * 示例：观察 services（你之后可以拆更细）
     */
    @OptIn(ExperimentalUuidApi::class)
    fun observeServices(): Flow<List<RemoteService>> {
        return peripheral
            .filterNotNull()
            .flatMapLatest { p ->
                p.services().filterNotNull()
            }
    }

//    /**
//     * 订阅指定 Service + Characteristic 的 notify 数据
//     */
//    fun observeNotifyData(
//        serviceUuid: UUID,
//        characteristicUuid: UUID
//    ): Flow<ByteArray> {
//
//        return centralManager
//            // 当前已连接的外设（断连会自动切）
//            .connectedPeripheral
//            .flatMapLatest { peripheral ->
//
//                // 监听 services 变化
//                peripheral.services()
//                    .filterNotNull()
//                    .mapNotNull { services ->
//                        services
//                            .firstOrNull { it.uuid == serviceUuid }
//                            ?.characteristics
//                            ?.firstOrNull { it.uuid == characteristicUuid }
//                    }
//                    .flatMapLatest { characteristic ->
//                        // 核心：订阅 notify
//                        characteristic.subscribe()
//                    }
//            }
//    }
}
