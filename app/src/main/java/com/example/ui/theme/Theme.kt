package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB3B8),
    onPrimary = Color(0xFF680016),
    primaryContainer = Color(0xFF8B0E23),
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = Color(0xFFE5B9C8),
    onSecondary = Color(0xFF442432),
    secondaryContainer = Color(0xFF5D3A48),
    onSecondaryContainer = Color(0xFFFFD9E6),
    tertiary = Color(0xFFFFB59C),
    background = Color(0xFF1B1113),
    onBackground = Color(0xFFEDE0DF),
    surface = Color(0xFF22181B),
    onSurface = Color(0xFFEDE0DF),
    surfaceVariant = Color(0xFF35252B),
    onSurfaceVariant = Color(0xFFD6C1C5)
)

private val LightColorScheme = lightColorScheme(
    primary = RubyHeart,
    onPrimary = Color.White,
    primaryContainer = LightRoseContainer,
    onPrimaryContainer = Color(0xFF410009),
    secondary = VelvetRose,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E6),
    onSecondaryContainer = Color(0xFF381021),
    tertiary = TerracottaSunset,
    background = SoftBackground,
    onBackground = DeepCharcoal,
    surface = SoftSurface,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = Color(0xFF534346)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom romantic aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
