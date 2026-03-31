package edu.nd.pmcburne.hello.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VirginiaAppLightMode = lightColorScheme(
    primary = VirginiaAppBlue,
    onPrimary = Color.White,
    secondary = VirginiaAppOrange,
    onSecondary = Color.White,
    background = VirginiaAppCream,
    onBackground = VirginiaAppBlue,
    surface = VirginiaBrightMode,
    onSurface = VirginiaAppBlue,
    surfaceVariant = VirginiaBrightModePanel,
    onSurfaceVariant = VirginiaAppBlue,
    outline = Color(0xFFB7AEA2)
)

private val VirginiaAppDarkMode = darkColorScheme(
    primary = VirginiaAppOrange,
    onPrimary = Color.Black,
    secondary = VirginiaAppBlue,
    onSecondary = Color.White,
    background = VirginiaBlackMode,
    onBackground = VirginiaBlackModeMessage,
    surface = VirginiaBlackModePanel,
    onSurface = VirginiaBlackModeMessage,
    surfaceVariant = VirginiaBlackModeSurface,
    onSurfaceVariant = VirginiaBlackModeMessage,
    outline = Color(0xFF94A3B8)
)

@Composable
fun VirginiaGroundsMapsTheme(
    darkModeEnabled: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkModeEnabled) VirginiaAppDarkMode else VirginiaAppLightMode,
        typography = Typography,
        content = content
    )
}