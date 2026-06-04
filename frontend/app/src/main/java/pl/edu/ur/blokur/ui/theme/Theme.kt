package pl.edu.ur.blokur.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = TextPrimary,

    secondary = SecondaryPurple,
    onSecondary = SurfaceLight,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = TextPrimary,

    tertiary = SuccessGreen,
    onTertiary = SurfaceLight,
    tertiaryContainer = SuccessGreenBg,
    onTertiaryContainer = TextPrimary,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = SurfaceLight
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

    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),

    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),

    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed
)

@Composable
fun PresentationTheme(
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