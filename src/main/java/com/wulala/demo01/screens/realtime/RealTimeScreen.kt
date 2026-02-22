package com.wulala.demo01.screens.realtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RealtimeScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // 深色背景
    ) {
        DataDisplayPane(
            modifier = Modifier
                .weight(0.72f)
                .fillMaxHeight()
        )

        ControlPanel(
            modifier = Modifier
                .weight(0.28f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun DataDisplayPane(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {

        // 图表区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF020617))
        ) {
            // TODO: 放 Canvas / Waveform / Heatmap
            Text(
                text = "Realtime Data View",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 数值叠加区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DataValue("CH1", "1.24 V")
            DataValue("CH2", "0.98 V")
            DataValue("Freq", "12.3 Hz")
            DataValue("RMS", "0.42")
        }
    }
}

@Composable
fun DataValue(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ControlPanel(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ConnectionCard()
        ControlCard()
        ModeCard()
        ParameterCard()
        Spacer(modifier = Modifier.weight(1f))
        SettingsButton()
    }
}

@Composable
fun ConnectionCard() {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Connection", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("● Connected", color = Color(0xFF16A34A))
        }
    }
}

@Composable
fun ControlCard() {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Acquisition", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* start */ }) {
                    Text("Start")
                }
                OutlinedButton(onClick = { /* stop */ }) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
fun ModeCard() {
    var mode by remember { mutableStateOf("Waveform") }

    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Mode", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            listOf("Waveform", "FFT", "Heatmap").forEach {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = mode == it,
                        onClick = { mode = it }
                    )
                    Text(it)
                }
            }
        }
    }
}

@Composable
fun ParameterCard() {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Parameters", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Gain: x1")
            Text("Rate: 1 kHz")
        }
    }
}

@Composable
fun SettingsButton() {
    Button(
        onClick = { /* open settings */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Settings")
    }
}





