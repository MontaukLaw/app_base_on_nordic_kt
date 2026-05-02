package com.wulala.demo01

import com.wulala.demo01.data.DATA_UUID
import com.wulala.demo01.data.HrFilterParams
import com.wulala.demo01.data.SYC_UUID_SERVICE_UUID
import com.wulala.demo01.repo.BleRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ObserveHeartRateSignalUseCase @Inject constructor(
    private val repository: BleRepository
) {

    private val SERVICE_UUID = Uuid.parse(SYC_UUID_SERVICE_UUID)
    private val CHAR_UUID = Uuid.parse(DATA_UUID)

    fun execute(params: HrFilterParams): Flow<FloatArray> {

        val hrChannel: Flow<FloatArray> = repository.observePressureValues(SERVICE_UUID, CHAR_UUID)
        return hrChannel
        /*
        return hrChannel
              .median(window = params.medianWindow)
              .lowPass(fsHz = params.fsHz, fcHz = params.lpHz)
        */
    }
}