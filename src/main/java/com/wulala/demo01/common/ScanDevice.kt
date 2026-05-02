package com.wulala.demo01.common
import no.nordicsemi.kotlin.ble.client.android.*;

data class ScanDevice(
    val id: String,          // peripheral.identifier (MAC)
    val name: String?,
    val rssi: Int,
    val isConnectable: Boolean,
    val lastSeenMs: Long,
    val peripheral: Peripheral,
    val advertisingData: AdvertisingData,  // 你后面要按 service UUID 过滤会用到
)