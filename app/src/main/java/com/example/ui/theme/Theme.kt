package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.healthogram.designsystem.HealthogramDesignSystemTheme

@Composable
fun HealthogramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    HealthogramDesignSystemTheme(
        darkTheme = darkTheme,
        content = content
    )
}

@Composable
@Deprecated("Use HealthogramTheme instead", ReplaceWith("HealthogramTheme(darkTheme, dynamicColor, content)"))
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    HealthogramTheme(darkTheme, dynamicColor, content)
}
