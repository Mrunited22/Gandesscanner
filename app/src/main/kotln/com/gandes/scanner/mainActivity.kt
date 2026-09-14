package com.gandes.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val GandesColors = darkColorScheme(
    primary = Color(0xFF00BCD4),
    background = Color(0xFF0F1115),
    surface = Color(0xFF161A22),
    onBackground = Color.White,
    onSurface = Color.White
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = GandesColors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HelloGandes()
                }
            }
        }
    }
}

@Composable
fun HelloGandes() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello Gandes",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Text(
            text = "App berhasil dibuka ✓",
            fontSize = 16.sp,
            color = Color.White
        )
    }
}
