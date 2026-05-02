package com.wulala.demo01.scanner

import androidx.lifecycle.ViewModel
import com.wulala.demo01.common.ScanDevice
import com.wulala.demo01.repo.BleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val repo: BleRepository
) : ViewModel() {

    val scanResults: StateFlow<List<ScanDevice>> = repo.scanResults
    val isScanning: StateFlow<Boolean> = repo.isScanning
    val peripheral: StateFlow<Peripheral?> = repo.peripheral

    // 直接用 Nordic 官方 state
    // val connectionState: Flow<ConnectionState> = repo.connectionState
    val connectionState = repo.connectionState

    // 可以输入关键字作为名称过滤
    fun startScan(nameContains: String) {
        // Timber.d("Start scan")
        val regex = Regex(".*${Regex.escape(nameContains)}.*", RegexOption.IGNORE_CASE)
        repo.startScan(timeout = 10.seconds) {
            Name(regex) // 你的 ConjunctionFilterScope DSL
        }
    }

    fun stopScan() = repo.stopScan()
    fun connect(p: Peripheral) = repo.connect(p) // ✅ 一行

}