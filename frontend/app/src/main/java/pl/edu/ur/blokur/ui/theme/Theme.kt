package pl.edu.ur.blokur.ui.theme

import androidx.compose.material3.MaterialTheme
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

@Composable
fun BlokurTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}