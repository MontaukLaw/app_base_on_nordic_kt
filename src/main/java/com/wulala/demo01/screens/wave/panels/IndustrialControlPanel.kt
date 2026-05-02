package com.wulala.demo01.screens.wave.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IndustrialControlPanel(
    amp:Float,
    onMedianWindowChange: (Int) -> Unit,
    onHistorySizeChange: (Int) -> Unit,
    onAmpChange: (Float) -> Unit,
) {

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        PanelTitle("Control")

        GainSection(onAmpChange, amp)

        ChannelSection()

        DividerLine()

        AcquisitionSection()

        DividerLine()

        DividerLine()

        TriggerSection()

        DividerLine()

        ActionButtons()

        DividerLine()

        StatusSection()

        Spacer(modifier = Modifier.height(40.dp))
    }
}
