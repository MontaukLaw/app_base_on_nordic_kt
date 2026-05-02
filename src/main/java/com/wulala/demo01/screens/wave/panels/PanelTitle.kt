package com.wulala.demo01.screens.wave.panels
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

@Composable
fun PanelTitle(text: String) {
    Text(
        text,
        color = Color(0xFFE0E0E0),
        style = MaterialTheme.typography.titleLarge
    )
}
