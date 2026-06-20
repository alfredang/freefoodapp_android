package com.tertiaryinfotech.freefood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// FreeFood brand green, taken from the iOS app icon gradient.
private val FreeFoodGreen = Color(0xFF2C8F5B)
private val FreeFoodGreenDark = Color(0xFF1A704D)
private val FreeFoodLeaf = Color(0xFF67BD5A)
private val FreeFoodCoral = Color(0xFFEF5E4A)

private val LightColors = lightColorScheme(
    primary = FreeFoodGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFEF82),
    onPrimaryContainer = Color(0xFF12351F),
    secondary = FreeFoodLeaf,
    tertiary = FreeFoodCoral,
    background = Color(0xFFFBFDF7),
    surface = Color(0xFFFBFDF7),
)

private val DarkColors = darkColorScheme(
    primary = FreeFoodLeaf,
    onPrimary = Color(0xFF06351E),
    primaryContainer = FreeFoodGreenDark,
    onPrimaryContainer = Color(0xFFCFEF82),
    secondary = FreeFoodLeaf,
    tertiary = FreeFoodCoral,
)

@Composable
fun FreeFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
