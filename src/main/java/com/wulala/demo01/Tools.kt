package com.wulala.demo01

import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

import kotlin.math.PI
import kotlin.math.abs
import kotlinx.coroutines.flow.*

object PressureFrameParser {

    // 按你描述推断：12 + 64 + 4 = 80
    const val TOTAL_SIZE = 80
    const val HEADER_SIZE = 12
    const val PAYLOAD_SIZE = 64
    const val CRC_SIZE = 4
    const val CHANNELS = 16

    data class Frame(
        val magic: Short,          // u16 -> Long
        val headerSize: Int,      // u16 -> Int
        val payloadLen: Long,      // u16 -> Int
        val frameId: Long,        // u32 -> Long
        val values: IntArray,   // 16通道压力
        val crcFromDevice: Long,  // u32 -> Long
        val crcCalculated: Long,  // u32 -> Long
        val crcOk: Boolean
    )

    /**
     * @param expectedMagic 你的 FRAME_MAGIC（不确定就先传 null 不校验）
     * @param strictLengths true: 强校验 headerSize/payloadLen；false: 只按固定位置解析
     */
    fun parse(
        packet: ByteArray,
        expectedMagic: Short? = null,
        strictLengths: Boolean = true
    ): Frame? {

        if (packet.size != TOTAL_SIZE) {
            Timber.w("Invalid packet size=${packet.size}")
            return null
        }

        // 小端：STM32 常见
        val bb = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)

        val magic = u16(bb.short)
        val headerSize = u16(bb.short)
        val payloadLen = u32(bb.int)
        val frameId = u32(bb.int)
        /*
        Timber.i(
            "payload raw = ${
                packet.copyOfRange(HEADER_SIZE, HEADER_SIZE + PAYLOAD_SIZE)
                    .joinToString(" ") { "%02X".format(it) }
            }"
        )
        */
        // Timber.d("Parsed header: magic=0x${magic.toString(16)}, headerSize=$headerSize, payloadLen=$payloadLen, frameId=$frameId")

        if (expectedMagic != null) {
            if (magic.toShort() != expectedMagic) {
                Timber.w("Bad magic=0x${magic.toString(16)}, expected=0x${expectedMagic.toString(16)}")
                return null
            }
        }

//         if (strictLengths) {
//          require(headerSize == HEADER_SIZE) { "Bad headerSize=$headerSize, expected=$HEADER_SIZE" }
//          require(payloadLen == PAYLOAD_SIZE) { "Bad payloadLen=$payloadLen, expected=$PAYLOAD_SIZE" }
//         }

        val payloadBuffer = ByteBuffer
            .wrap(packet, HEADER_SIZE, PAYLOAD_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)

        val u32Values = IntArray(CHANNELS)
        for (i in 0 until CHANNELS) {
            u32Values[i] = payloadBuffer.int // 16个 u32
        }

        // Timber.i("u32 values=${u32Values.joinToString()}")

        // val crcFromDevice = u32(bb.int)
        val crcFromDevice = u32(
            ByteBuffer.wrap(packet)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt(HEADER_SIZE + PAYLOAD_SIZE)
        )
        // 计算CRC覆盖范围：header + payload
        val crcCalculated = crc32_ieee(packet, 0, HEADER_SIZE + PAYLOAD_SIZE)

        return Frame(
            magic = magic.toShort(),
            headerSize = headerSize,
            payloadLen = payloadLen,
            frameId = frameId,
            values = u32Values,
            crcFromDevice = crcFromDevice,
            crcCalculated = crcCalculated,
            crcOk = (crcFromDevice == crcCalculated)
        )
    }

    /** Timber 打印一帧（你可按需改格式） */
    fun logFrame(tag: String = "BLE", f: Frame) {

        val vals = f.values.joinToString(prefix = "[", postfix = "]") {
            it.toString() // 10进制
        }
        // Timber.i("$tag Frame: id=${f.frameId}, crcOk=${f.crcOk}, values=$vals")
    }

    // ---------- helpers ----------

    private fun u16(v: Short): Int = v.toInt() and 0xFFFF
    private fun u32(v: Int): Long = v.toLong() and 0xFFFF_FFFFL

    /**
     * CRC-32/IEEE (Ethernet, zip):
     * poly=0xEDB88320 (reflected), init=0xFFFFFFFF, xorout=0xFFFFFFFF
     */
    private fun crc32_ieee(data: ByteArray, offset: Int, len: Int): Long {
        var crc = 0xFFFF_FFFFL
        for (i in offset until (offset + len)) {
            crc = crc xor (data[i].toLong() and 0xFF)
            for (b in 0 until 8) {
                crc = if ((crc and 1L) != 0L) {
                    (crc ushr 1) xor 0xEDB88320L
                } else {
                    crc ushr 1
                }
            }
        }
        crc = crc xor 0xFFFF_FFFFL
        return crc and 0xFFFF_FFFFL
    }
}

fun Flow<Float>.median(window: Int = 5): Flow<Float> {

    val w = if (window < 3 || window % 2 == 0) 5 else window

    data class S(
        val buf: FloatArray,
        val idx: Int,
        val filled: Int
    )

    return scan(S(FloatArray(w), 0, 0)) { s, x ->

        val buf = s.buf.copyOf()

        val idx = if (w == 0) 0 else s.idx % w

        if (w > 0) {
            buf[idx] = x
        }

        val nextIdx = (idx + 1) % w
        val nextFilled = if (s.filled < w) s.filled + 1 else w

        S(buf, nextIdx, nextFilled)
    }
        .map { s ->
            if (s.filled == 0) return@map 0f

            val tmp = s.buf.copyOf(s.filled)
            tmp.sort()
            tmp[tmp.size / 2]
        }
}

// ---------- 2) First-order High-pass ----------
fun Flow<Float>.highPass(fsHz: Float, fcHz: Float): Flow<Float> {
    val dt = 1f / fsHz
    val rc = 1f / (2f * PI.toFloat() * fcHz)
    val alpha = rc / (rc + dt)

    data class S(var y: Float = 0f, var xPrev: Float = 0f)

    return scan(S()) { s, x ->
        val y = alpha * (s.y + x - s.xPrev)
        s.y = y
        s.xPrev = x
        s
    }.map { it.y }
}

// ---------- 3) First-order Low-pass ----------
fun Flow<Float>.lowPass(fsHz: Float, fcHz: Float): Flow<Float> {
    val dt = 1f / fsHz
    val rc = 1f / (2f * PI.toFloat() * fcHz)
    val alpha = dt / (rc + dt)

    data class S(var y: Float = 0f, var inited: Boolean = false)

    return scan(S()) { s, x ->
        val y = if (!s.inited) {
            s.inited = true
            x
        } else {
            s.y + alpha * (x - s.y)
        }
        s.y = y
        s
    }.map { it.y }
}