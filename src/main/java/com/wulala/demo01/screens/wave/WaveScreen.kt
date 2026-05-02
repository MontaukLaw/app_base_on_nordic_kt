package com.wulala.demo01.screens.wave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wulala.demo01.data.HrFilterParams
import com.wulala.demo01.mainpage.MainViewModel
import com.wulala.demo01.screens.wave.panels.IndustrialControlPanel
import com.wulala.demo01.screens.wave.panels.WaveformView

@Composable
fun WaveScreen(vm: MainViewModel = hiltViewModel()) {
    val wave by vm.waveform.collectAsStateWithLifecycle()
    val amp by vm.amp.collectAsStateWithLifecycle()
    // Text("$wave[0]")
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // LEFT 75% Wave
        Box(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxHeight()
        ) {
            WaveformView(wave, amp)
        }

        // RIGHT 25% Controls
        Box(
            modifier = Modifier
                .weight(0.20f)
                .fillMaxHeight()
        ) {

            IndustrialControlPanel(
                amp = amp,
                onAmpChange = vm::setSlider,
                onMedianWindowChange = vm::setMedianWindow,
                onHistorySizeChange = vm::setHistorySize,
            )
        }
    }
}