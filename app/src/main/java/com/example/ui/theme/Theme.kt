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
    primary = WarmTerracottaPrimaryDark,
    onPrimary = Color(0xFF4A1505),
    primaryContainer = WarmTerracottaContainerDark,
    onPrimaryContainer = Color(0xFFFFD8CC),
    secondary = WarmEspressoSecondaryDark,
    onSecondary = Color(0xFF231713),
    secondaryContainer = WarmSandContainerDark,
    onSecondaryContainer = Color(0xFFF3E9E2),
    tertiary = HoneyAmberTertiaryDark,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = HoneyAmberContainerDark,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = BackgroundWarmDark,
    onBackground = Color(0xFFFAF7F2),
    surface = SurfaceWarmDark,
    onSurface = Color(0xFFFAF7F2),
    surfaceVariant = SurfaceVariantWarmDark,
    onSurfaceVariant = Color(0xFFD6CBC5)
)

private val LightColorScheme = lightColorScheme(
    primary = WarmTerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = WarmTerracottaContainer,
    onPrimaryContainer = Color(0xFF4A1505),
    secondary = WarmEspressoSecondary,
    onSecondary = Color.White,
    secondaryContainer = WarmSandContainer,
    onSecondaryContainer = Color(0xFF231713),
    tertiary = HoneyAmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = HoneyAmberContainer,
    onTertiaryContainer = Color(0xFF451A03),
    background = BackgroundWarmLight,
    onBackground = Color(0xFF231713),
    surface = SurfaceWarmLight,
    onSurface = Color(0xFF231713),
    surfaceVariant = SurfaceVariantWarmLight,
    onSurfaceVariant = Color(0xFF5A4B45)
)

@Composable
fun SplitNotaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Păstrăm paleta caldă și armonioasă indiferent de setările de sistem
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
