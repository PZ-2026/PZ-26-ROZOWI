package pl.edu.ur.blokur.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = TextPrimary,

    secondary = SecondaryPurple,
    onSecondary = SurfaceLight,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = TextPrimary,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = SurfaceLight
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