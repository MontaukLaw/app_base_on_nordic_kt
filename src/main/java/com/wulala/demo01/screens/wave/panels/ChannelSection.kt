package com.wulala.demo01.screens.wave.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChannelSection() {

    var selected by remember { mutableStateOf("CH1") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        SectionTitle("Channel")

        listOf("CH1", "CH2", "CH3").forEach { ch ->

            Row(verticalAlignment = Alignment.CenterVertically) {

                RadioButton(
                    selected = selected == ch,
                    onClick = { selected = ch }
                )

                Text(ch, color = Color(0xFFE0E0E0))
            }
        }
    }
}
