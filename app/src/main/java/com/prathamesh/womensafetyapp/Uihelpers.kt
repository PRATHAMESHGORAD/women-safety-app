package com.prathamesh.womensafetyapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val CelestialGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F1B3A), Color(0xFF2E406F))
)

@Composable
fun CelestialBackground(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(CelestialGradient),
        color = Color.Transparent
    ) {
        content()
    }
}
