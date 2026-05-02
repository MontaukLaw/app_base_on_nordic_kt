package com.wulala.demo01.screens.wave.panels
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        color = Color(0xFFB0BEC5),
        style = MaterialTheme.typography.titleMedium
    )
}
