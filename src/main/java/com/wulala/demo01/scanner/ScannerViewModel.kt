package com.wulala.demo01.scanner

import androidx.lifecycle.ViewModel
import com.wulala.demo01.repo.BleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.kotlin.ble.client.android.Peripheral

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val bleRepository: BleRepository
) : ViewModel() {

    // 当前连接设备（给 UI 用）
    val connectedPeripheral: StateFlow<Peripheral?> =
        bleRepository.peripheral

    fun onPeripheralSelected(peripheral: Peripheral) {
        bleRepository.toggleConnection(peripheral)
    }
}