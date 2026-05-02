package com.wulala.demo01.repo

import com.wulala.demo01.PressureFrameParser
import com.wulala.demo01.PressureFrameParser.logFrame
import com.wulala.demo01.common.ScanDevice
import com.wulala.demo01.highPass
import com.wulala.demo01.lowPass
import com.wulala.demo01.median
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeoutOrNull
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber


@Singleton
class BleRepository @Inject constructor(
    private val centralManager: CentralManager, private val appScope: CoroutineScope
) {

    // 扫描错误
    private val _scanError = MutableStateFlow<Throwable?>(null)
    val scanError: StateFlow<Throwable?> = _scanError.asStateFlow()

    private val scanCache = mutableMapOf<String, ScanDevice>()
    private val cacheLock = Any()
    private var publishJob: Job? = null

    private val defaultOptions = CentralManager.ConnectionOptions.Direct(
        timeout = 10.seconds, retry = 2, retryDelay = 300.milliseconds, preferredPhy = listOf(Phy.PHY_LE_1M), automaticallyRequestHighestValueLength = true
    )

    private var connectJob: Job? = null
    private var disconnectJob: Job? = null

    // ----------------------------
    // Selected / current peripheral
    // ----------------------------
    private val _peripheral = MutableStateFlow<Peripheral?>(null)
    val peripheral: StateFlow<Peripheral?> = _peripheral.asStateFlow()

    fun selectPeripheralById(id: String) {
        val p = centralManager.getPeripheralById(id)
        _peripheral.value = p
    }

    val connectionState: StateFlow<ConnectionState> = peripheral.filterNotNull().flatMapLatest { it.state }.stateIn(appScope, SharingStarted.WhileSubscribed(5_000), ConnectionState.Disconnected(null))

    // ----------------------------
    // Scan results
    // ----------------------------
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScanDevice>>(emptyList())
    val scanResults: StateFlow<List<ScanDevice>> = _scanResults.asStateFlow()

    private var scanJob: Job? = null

    /**
     * 开始扫描（重复调用会先 stop 再 start）
     *
     * @param timeout 扫描时长，默认无限（直到 stopScan 或 collector 取消）
     * @param pruneOlderThan 多久没出现就从列表移除（UI 更干净）
     */
    fun startScan(
        timeout: Duration = Duration.INFINITE, publishIntervalMs: Long = 500L, pruneOlderThan:
        Duration = 8.seconds, onlyConnectable: Boolean = true, filter: ConjunctionFilterScope.() -> Unit = {}
    ) {

        stopScan(clearResults = false)

        _isScanning.value = true
        _scanResults.value = emptyList()

        // ✅ 定时发布 UI 列表（节流）
        publishJob = appScope.launch {
            try {
                while (isActive) {
                    delay(publishIntervalMs)
                    val snapshot = synchronized(cacheLock) {
                        val cutoff = System.currentTimeMillis() - pruneOlderThan.inWholeMilliseconds
                        scanCache.values.filter { it.lastSeenMs >= cutoff }.sortedWith(compareByDescending<ScanDevice> { it.rssi }.thenBy { it.name ?: "" }).toList()
                    }
                    _scanResults.value = snapshot
                }
            } catch (_: CancellationException) {
            }
        }

        // 1) 扫描收集
        scanJob = appScope.launch {
            try {
                centralManager.scan(timeout, filter).collect { sr ->
                    if (onlyConnectable && !sr.isConnectable) return@collect
                    // 高频写 cache，不直接更新 _scanResults
                    upsertScanCache(sr)
                }
            } catch (_: CancellationException) {
            } finally {
                _isScanning.value = false
            }
        }
    }

    private fun upsertScanCache(sr: ScanResult) {
        val p = sr.peripheral
        val id = p.identifier
        val now = System.currentTimeMillis()   // ✅ 用 wall-clock

        synchronized(cacheLock) {
            val old = scanCache[id]
            scanCache[id] = ScanDevice(
                id = id, name = p.name ?: old?.name, rssi = sr.rssi, isConnectable = sr.isConnectable, lastSeenMs = now, peripheral = p, advertisingData = sr.advertisingData
            )
        }
    }

    fun stopScan(clearResults: Boolean = false) {
        scanJob?.cancel()
        publishJob?.cancel()

        scanJob = null
        publishJob = null

        synchronized(cacheLock) { scanCache.clear() }

        _isScanning.value = false
        if (clearResults) _scanResults.value = emptyList()
    }

    fun connect(p: Peripheral) {
        stopScan(clearResults = false)
        connectJob?.cancel()
        connectJob = appScope.launch {
            _peripheral.value = p
            centralManager.connect(p, defaultOptions)

        }
    }

    fun connect(p: Peripheral, options: CentralManager.ConnectionOptions.Direct) {

        // 连接之前停止扫描了就
        stopScan(clearResults = false)

        connectJob?.cancel()
        connectJob = appScope.launch {
            // 关键：先设置，UI 才能立刻看到 state=Disconnected(null)->Connecting...
            _peripheral.value = p
            try {
                centralManager.connect(p, defaultOptions)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 连接失败：可选择清掉 peripheral，或保留让 UI 看 Disconnected(reason)
                // 我更推荐：保留 p，让 UI 能读到 Disconnected(reason=Timeout/Cancelled/Unknown...)
                // 但如果 centralManager.connect 直接抛异常而不是靠 state，你可以清掉：
                // _peripheral.value = null
            }
        }
    }

    fun disconnect() {
        val p = _peripheral.value ?: return
        appScope.launch {
            try {
                // 你源码注释里写的：Disconnecting 是在 p.disconnect() 调用时设置
                p.disconnect()
            } finally {
                // 这里是否清空 peripheral 取决于你的 UX：
                // - 如果你想 UI 继续显示“上一次设备 + 断开原因”，就不要清
                // - 如果你想断开就回到“未选择任何设备”，就清空
                // _peripheral.value = null
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun observeServices(): Flow<List<RemoteService>> {
        return peripheral.filterNotNull().flatMapLatest { p ->
            p.services().filterNotNull()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun observeCharacteristic(
        serviceUuid: Uuid, charUuid: Uuid
    ): Flow<ByteArray> {
        return peripheral.filterNotNull().flatMapLatest { p ->
            Timber.i("BLE: observeCharacteristic for ${p.identifier}, state=${p.state.value}")

            p.services().onStart { Timber.i("BLE: services() collecting -> should trigger discovery") }.onEach { s -> Timber.i("BLE: services emitted = ${s?.size ?: -1}") }.filterNotNull().timeout(10.seconds).map { services ->
                val svc = services.firstOrNull { it.uuid == serviceUuid } ?: error("Service not found: $serviceUuid, found=${services.map { it.uuid }}")

                val ch = svc.characteristics.firstOrNull { it.uuid == charUuid } ?: error("Char not found: $charUuid, found=${svc.characteristics.map { it.uuid }}")

                Timber.i("BLE: found service+char OK. props=${ch.properties}")
                ch
            }.take(1).flatMapLatest { ch ->
                runCatching { ch.read() }.onSuccess { Timber.i("BLE: read OK len=${it.size}") }.onFailure { Timber.e(it, "BLE: read failed") }

                // ✅ 只订阅一次
                val gotFirst = java.util.concurrent.atomic.AtomicBoolean(false)

                ch.subscribe().onStart {
                    Timber.i("BLE: subscribing to $charUuid ...")

                    // ✅ 不二次 collect：在同一个订阅里开一个计时器
                    gotFirst.set(false)
                    appScope.launch {
                        delay(3_000)
                        if (!gotFirst.get()) {
                            Timber.i("BLE: first notify within 3s = -1")
                        }
                    }
                }.onEach { bytes ->
                    if (gotFirst.compareAndSet(false, true)) {
                        Timber.i("BLE: first notify within 3s = ${bytes.size}")
                    }
                    Timber.i("BLE: notify len=${bytes.size}")
                }.catch { e ->
                    Timber.e(e, "BLE: subscribe error")
                    throw e
                }.onCompletion { Timber.w("BLE: subscribe flow completed") }
            }
        }.catch { e ->
            Timber.e(e, "BLE: observeCharacteristic failed")
            throw e
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun observeNotify(
        serviceUuid: Uuid, charUuid: Uuid
    ): Flow<ByteArray> {
        return peripheral.filterNotNull().flatMapLatest { p ->
            Timber.i("BLE: observeNotify ${p.identifier}, state=${p.state.value}")

            p.services().filterNotNull().map { services ->
                val svc = services.firstOrNull { it.uuid == serviceUuid } ?: error("Service not found: $serviceUuid")

                val ch = svc.characteristics.firstOrNull { it.uuid == charUuid } ?: error("Char not found: $charUuid")

                ch
            }.take(1) // 只找一次特征
                .flatMapLatest { ch ->
                    ch.subscribe().onStart { Timber.i("BLE: subscribe start $charUuid") }.onEach { bytes ->

                        val framesFlow = PressureFrameParser.parse(bytes, 0x55AA)?.let { logFrame("BLE", it) }

                    }.onCompletion { Timber.w("BLE: subscribe completed $charUuid") }
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun observePressureValues(
        serviceUuid: Uuid, charUuid: Uuid
    ): Flow<FloatArray> = observeFrames(serviceUuid, charUuid)
        .filter { it.crcOk }
        .map {
            if (it.values.isEmpty()) {
                Timber.w("Frame has no values")
                return@map FloatArray(0)
            }
            Timber.i("BLE: ${it.values[0]} ${it.values[1]} ${it.values[2]} ${it.values[3]}")

            // ✅ 直接转 float，除以当初乘的值（比如 1000000），避免后续处理都要除
            FloatArray(it.values.size) { idx ->
                it.values[idx].toFloat() / 1_000_000f
            }

            // Timber.i("BLE: got frame with ${it.values.size} values, first=${it.values[0]}")
            // new FloatArray(2){
            //    it.values[0].toFloat() / 1000000,
            //    it.values[2].toFloat() / 1000000
            // }

        }  // 转为float格式,并除以当初乘的值

    @OptIn(ExperimentalUuidApi::class)
    fun observeFrames(
        serviceUuid: Uuid, charUuid: Uuid
    ): Flow<PressureFrameParser.Frame> {
        return peripheral.filterNotNull().flatMapLatest { p ->
            p.services().filterNotNull().map { services ->
                val svc = services.firstOrNull { it.uuid == serviceUuid } ?: error("Service not found: $serviceUuid")
                val ch = svc.characteristics.firstOrNull { it.uuid == charUuid } ?: error("Char not found: $charUuid")
                ch
            }.take(1).flatMapLatest { ch ->
                ch.subscribe()
            }
        }.mapNotNull { bytes ->
            // parse 返回 Frame? （长度不对/解析失败 -> null 丢掉）
            PressureFrameParser.parse(bytes, expectedMagic = 0x55AA, true)
        }.onEach { frame ->
            // 这里可以选择性打印（但不影响返回）
            PressureFrameParser.logFrame("BLE", frame)
        }
    }
}
