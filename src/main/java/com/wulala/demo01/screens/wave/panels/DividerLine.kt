package com.wulala.demo01.screens.wave.panels
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DividerLine() {
    HorizontalDivider(
        color = Color(0xFF2F2F2F),
        thickness = 1.dp
    )
}
