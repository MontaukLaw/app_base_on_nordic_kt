package com.wulala.demo01.screens.wave.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/*
@Composable
fun WaveformView_(wave: Array<FloatArray>, amp: Float) {

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        val width = size.width
        val height = size.height

        // -------- GRID --------
        val gridColor = Color(0xFF2F2F2F)

        val verticalLines = 10
        val horizontalLines = 8

        for (i in 0..verticalLines) {
            val x = width / verticalLines * i
            drawLine(
                gridColor,
                Offset(x, 0f),
                Offset(x, height),
                1f
            )
        }

        for (i in 0..horizontalLines) {
            val y = height / horizontalLines * i
            drawLine(
                gridColor,
                Offset(0f, y),
                Offset(width, y),
                1f
            )
        }

        val points = wave.size
        if (points < 2) return@Canvas

        val padY = 16f
        val padX = 0f

        val minV = wave.minOrNull() ?: 0f
        val maxV = wave.maxOrNull() ?: 1f
        val range = (maxV - minV).let { if (it == 0f) 1f else it }

        // X step：注意 points-1，避免最后一个点画不到最右边
        val step = (width - padX * 2) / (points - 1)

        var prev = Offset(padX, 0f)

        for (i in 0 until points) {
            val x = padX + i * step

            // 归一化到 0..1
            val t = (wave[i] - minV) / range

            // 映射到画布：t=0 -> bottom，t=1 -> top（所以 1 - t）
            val y = padY + (1f - t) * (height - padY * 2)

            val cur = Offset(x, y)

            if (i > 0) {
                drawLine(
                    color = Color.Green,
                    start = prev,
                    end = cur,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            prev = cur
        }
    }
}
*/


@Composable
fun WaveformView(
    wave: Array<FloatArray>,
    amp: Float
) {

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        val width = size.width
        val height = size.height

        // -------- GRID --------
        val gridColor = Color(0xFF2F2F2F)

        val verticalLines = 10
        val horizontalLines = 8

        for (i in 0..verticalLines) {
            val x = width / verticalLines * i
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), 1f)
        }

        for (i in 0..horizontalLines) {
            val y = height / horizontalLines * i
            drawLine(gridColor, Offset(0f, y), Offset(width, y), 1f)
        }

        if (wave.isEmpty()) return@Canvas

        val padY = 16f
        val padX = 0f

        // ========= 统一Y范围（所有通道一起算） =========
        var globalMin = Float.MAX_VALUE
        var globalMax = Float.MIN_VALUE

        wave.forEach { arr ->
            arr.forEach {
                if (it < globalMin) globalMin = it
                if (it > globalMax) globalMax = it
            }
        }

        if (globalMin == Float.MAX_VALUE) return@Canvas

        val range = (globalMax - globalMin).let {
            if (it == 0f) 1f else it
        }

        // ========= 画通道 =========
        val colors = listOf(
            Color.Green,
            Color.Cyan
        )

        wave.forEachIndexed { chIndex, channel ->

            if (channel.size < 2) return@forEachIndexed

            val points = channel.size
            val step = (width - padX * 2) / (points - 1)

            var prev = Offset.Zero

            for (i in 0 until points) {

                val x = padX + i * step

                val value = channel[i] * amp

                val t = (value - globalMin) / range
                val y = padY + (1f - t) * (height - padY * 2)

                val cur = Offset(x, y)

                if (i > 0) {
                    drawLine(
                        color = colors[chIndex % colors.size],
                        start = prev,
                        end = cur,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                prev = cur
            }
        }
    }
}