package pl.edu.ur.blokur.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Indigo600,
    onPrimary = White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo700,

    secondary = Amber600,
    onSecondary = White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber600,

    tertiary = SuccessGreen,
    onTertiary = White,
    tertiaryContainer = SuccessGreenBg,
    onTertiaryContainer = SuccessGreen,

    background = NeutralBg,
    onBackground = TextPrimary,

    surface = NeutralSurface,
    onSurface = TextPrimary,
    surfaceVariant = NeutralSurface2,
    onSurfaceVariant = TextSecondary,

    outline = Stroke,
    outlineVariant = StrokeLight,

    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = Indigo100,
    onPrimary = Indigo700,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo50,

    secondary = Amber600,
    onSecondary = White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber600,

    tertiary = SuccessGreen,
    onTertiary = White,
    tertiaryContainer = SuccessGreenBg,
    onTertiaryContainer = SuccessGreen,

    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed
)

@Composable
fun BlokurTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}