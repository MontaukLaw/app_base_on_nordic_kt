package com.wulala.demo01.mainpage

import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wulala.demo01.ObserveHeartRateSignalUseCase
import com.wulala.demo01.data.*
import com.wulala.demo01.repo.BleRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import kotlin.uuid.ExperimentalUuidApi


@HiltViewModel
@OptIn(ExperimentalUuidApi::class)
class MainViewModel @Inject constructor(
    private val repo: BleRepository,
    private val useCase: ObserveHeartRateSignalUseCase
) : ViewModel() {

    private data class WaveBuf2(
        val ch0: FloatArray,
        val ch2: FloatArray,
        val writeIdx: Int,
        val filled: Int
    )

    val peripheral: StateFlow<Peripheral?> = repo.peripheral

    private val _ampSlider = MutableStateFlow(0.5f)
    val amp: StateFlow<Float> = _ampSlider

    fun setSlider(v: Float) {
        _ampSlider.value = v
    }

    val connectionState = repo.connectionState

    fun disconnect() = repo.disconnect()
    private val _subState = MutableStateFlow<CharSubState>(CharSubState.Idle)
    val subState: StateFlow<CharSubState> = _subState

    fun updateParams(params: HrFilterParams) {
        paramsFlow.value = params
    }

    private val paramsFlow = MutableStateFlow(HrFilterParams())

    fun setMedianWindow(w: Int) {
        paramsFlow.update { it.copy(medianWindow = w) }
    }

    fun setHistorySize(s: Int) {
        paramsFlow.update { it.copy(historySize = s) }
    }

    val filteredSignal =
        paramsFlow.flatMapLatest { params -> useCase.execute(params) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    /*
    private fun Flow<FloatArray>.toWaveWindow_(size: Int): Flow<FloatArray> {
        val n = size.coerceAtLeast(8) // 给个最小值，避免 0/1 崩
        return scan(WaveBuf(FloatArray(n), 0, 0)) { s, x ->
            val arr = s.data
            arr[s.writeIdx] = x
            val nextIdx = (s.writeIdx + 1) % n
            val nextFilled = if (s.filled < n) s.filled + 1 else n
            WaveBuf(arr, nextIdx, nextFilled)
        }.map { s ->
            // 输出为“按时间从旧到新排列”的数组，方便画波形
            val out = FloatArray(s.filled)
            val start = if (s.filled < s.data.size) 0 else s.writeIdx
            for (i in 0 until s.filled) {
                out[i] = s.data[(start + i) % s.data.size]
            }
            out
        }
    }


    val waveform: StateFlow<Array<FloatArray>> =
        paramsFlow
            .flatMapLatest { params ->
                useCase.execute(params)
                    .toWaveWindow(params.historySize)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FloatArray(0))
    */

    val waveform: StateFlow<Array<FloatArray>> =
        paramsFlow
            .flatMapLatest { params ->
                useCase.execute(params)
                    .toWaveWindow(params.historySize)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                arrayOf(FloatArray(0), FloatArray(0))
            )

    private fun Flow<FloatArray>.toWaveWindow(size: Int): Flow<Array<FloatArray>> {

        val n = size.coerceAtLeast(8)

        return scan(
            WaveBuf2(
                FloatArray(n),
                FloatArray(n),
                0,
                0
            )
        ) { s, x ->

            val i = s.writeIdx

            // 取 idx 0 和 idx 2
            val v0 = x.getOrNull(0) ?: 0f
            val v2 = x.getOrNull(2) ?: 0f

            s.ch0[i] = v0
            s.ch2[i] = v2

            val nextIdx = (i + 1) % n
            val nextFilled = if (s.filled < n) s.filled + 1 else n

            WaveBuf2(
                s.ch0,
                s.ch2,
                nextIdx,
                nextFilled
            )
        }
            .map { s ->

                val out0 = FloatArray(s.filled)
                val out2 = FloatArray(s.filled)

                val start =
                    if (s.filled < s.ch0.size) 0
                    else s.writeIdx

                for (i in 0 until s.filled) {

                    val idx = (start + i) % s.ch0.size

                    out0[i] = s.ch0[idx]
                    out2[i] = s.ch2[idx]
                }

                arrayOf(out0, out2)
            }
    }

}