package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary              = BEngelGreen,
    onPrimary            = Color.White,
    primaryContainer     = DarkSurfaceVar,
    onPrimaryContainer   = BEngelAccent,
    secondary            = BEngelAccent,
    onSecondary          = DarkBg,
    secondaryContainer   = DarkSurface,
    onSecondaryContainer = OnDarkPrimary,
    background           = DarkBg,
    onBackground         = OnDarkPrimary,
    surface              = DarkSurface,
    onSurface            = OnDarkPrimary,
    surfaceVariant       = DarkSurfaceVar,
    onSurfaceVariant     = OnDarkSecondary,
    outline              = DarkBorder,
    error                = StatusBlocked,
    onError              = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary              = BEngelGreen,
    onPrimary            = Color.White,
    primaryContainer     = LightSurfaceVar,
    onPrimaryContainer   = BEngelGreenDark,
    secondary            = BEngelGreenLight,
    onSecondary          = Color.White,
    background           = LightBg,
    onBackground         = OnLightPrimary,
    surface              = LightSurface,
    onSurface            = OnLightPrimary,
    surfaceVariant       = LightSurfaceVar,
    onSurfaceVariant     = OnLightSecondary,
    error                = StatusBlocked,
    onError              = Color.White,
)

@Composable
fun CallShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
