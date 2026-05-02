package com.wulala.demo01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.wulala.demo01.routes.AppNavHost
import com.wulala.demo01.ui.theme.KotlinBLELibraryTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.WindowCompat
import com.wulala.demo01.ui.theme.ScopeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinBLELibraryTheme {
                AppNavHost()
            }
        }
    }
}