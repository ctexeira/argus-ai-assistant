package com.argus.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArgusColorScheme = lightColorScheme(
    primary = Color(0xFF00CED1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF009FAE),
    onPrimaryContainer = Color.White,
)

@Composable
fun ArgusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArgusColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
} 