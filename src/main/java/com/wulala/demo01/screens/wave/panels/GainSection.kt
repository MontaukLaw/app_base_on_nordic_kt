package com.wulala.demo01.screens.wave.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GainSection(onAmpChange: (Float) -> Unit, amp:Float) {

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        SectionTitle("Gain")

        Slider(
            value = amp,
            onValueChange = onAmpChange
        )

        Text(
            "Gain: ${(amp * 10).toInt()} x",
            color = Color(0xFFB0BEC5)
        )
    }
}
