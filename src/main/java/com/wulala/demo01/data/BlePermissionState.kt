package com.wulala.demo01.data

sealed class BlePermissionState {
    object Granted : BlePermissionState()
    object Denied : BlePermissionState()
    object PermanentlyDenied : BlePermissionState()
}
