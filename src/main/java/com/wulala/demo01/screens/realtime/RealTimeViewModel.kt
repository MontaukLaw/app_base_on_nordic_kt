package com.wulala.demo01.screens.realtime

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import javax.inject.Inject

@HiltViewModel
class RealTimeViewModel @Inject constructor( // @Inject constructor(...) —— “我需要这些东西，你给我准备好”
    // 真正干活的人, 即CentralManager实例, 由Hilt提供
    // 第一次被“需要”的时候，由 Hilt 创建
    private val centralManager: CentralManager,
) : ViewModel() {

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()
}