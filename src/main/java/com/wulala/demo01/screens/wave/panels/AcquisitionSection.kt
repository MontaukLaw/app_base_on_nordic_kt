package com.wulala.demo01.screens.wave.panels

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AcquisitionSection() {

    var enabled by remember { mutableStateOf(true) }
    var freq by remember { mutableStateOf("1000") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        SectionTitle("Acquisition")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable", color = Color(0xFFE0E0E0))
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it }
            )
        }

        OutlinedTextField(
            value = freq,
            onValueChange = { freq = it },
            label = { Text("Frequency (Hz)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
