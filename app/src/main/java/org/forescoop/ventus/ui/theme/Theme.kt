package org.forescoop.ventus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VentusBlue80,
    secondary = VentusBlueGrey80,
    tertiary = VentusSky80
)

private val LightColorScheme = lightColorScheme(
    primary = VentusBlue40,
    secondary = VentusBlueGrey40,
    tertiary = VentusSky40
)

/// Mirrors the intent of AppAppearanceMode + VentusTheme on iOS (Sources/
/// Forescoop/Classes/View/Settings/{AppAppearanceMode,VentusTheme}.swift):
/// System/Light/Dark plus a swappable color theme. For the bare scaffold
/// this is just System (via `isSystemInDarkTheme`) with dynamic color on
/// Android 12+; the System/Light/Dark override and the named palettes
/// (Ocean, Forest, Sunset, …) come later.
@Composable
fun VentusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
