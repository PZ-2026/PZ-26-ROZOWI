package pl.edu.ur.blokur.ui.android.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal object AppPalete {
    // Primary blues
    val PrimaryBlue = Color(0xFF1D4ED8)
    val PrimaryContainer = Color(0xFFDCEAFE)

    // Indigo palette
    val Indigo50 = Color(0xFFEEF2FF)
    val Indigo100 = Color(0xFFE0E7FF)
    val Indigo700 = Color(0xFF4338CA)

    // Secondary purples
    val SecondaryPurple = Color(0xFF7C3AED)
    val SecondaryContainer = Color(0xFFEDE9FE)

    // Amber palette
    val Amber100 = Color(0xFFFEF3C7)
    val Amber600 = Color(0xFFD97706)

    // Backgrounds & surfaces
    val BackgroundLight = Color(0xFFF5F7FB)
    val NeutralBg = Color(0xFFF5F7FB)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantLight = Color(0xFFE5E7EB)
    val White = Color(0xFFFFFFFF)

    // Gradient
    val GradientStart = Color(0xFF4338CA)
    val GradientEnd = Color(0xFF1D4ED8)

    // Text
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)

    // Semantic
    val SuccessGreen = Color(0xFF059669)
    val SuccessGreenBg = Color(0xFFD1FAE5)
    val WarningOrange = Color(0xFFD97706)
    val ErrorRed = Color(0xFFDC2626)
    val ErrorRedBg = Color(0xFFFEE2E2)
    val InfoBlue = Color(0xFF2563EB)
}

val Shapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp)
)

val Typography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
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

private val LightColors = lightColorScheme(
    primary = AppPalete.PrimaryBlue,
    onPrimary = AppPalete.SurfaceLight,
    primaryContainer = AppPalete.PrimaryContainer,
    onPrimaryContainer = AppPalete.TextPrimary,

    secondary = AppPalete.SecondaryPurple,
    onSecondary = AppPalete.SurfaceLight,
    secondaryContainer = AppPalete.SecondaryContainer,
    onSecondaryContainer = AppPalete.TextPrimary,

    background = AppPalete.BackgroundLight,
    onBackground = AppPalete.TextPrimary,

    surface = AppPalete.SurfaceLight,
    onSurface = AppPalete.TextPrimary,
    surfaceVariant = AppPalete.SurfaceVariantLight,
    onSurfaceVariant = AppPalete.TextSecondary,

    error = AppPalete.ErrorRed,
    onError = AppPalete.SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = AppPalete.Indigo100,
    onPrimary = AppPalete.Indigo700,
    primaryContainer = AppPalete.Indigo700,
    onPrimaryContainer = AppPalete.Indigo50,

    secondary = AppPalete.Amber600,
    onSecondary = AppPalete.White,
    secondaryContainer = AppPalete.Amber100,
    onSecondaryContainer = AppPalete.Amber600,

    tertiary = AppPalete.SuccessGreen,
    onTertiary = AppPalete.White,
    tertiaryContainer = AppPalete.SuccessGreenBg,
    onTertiaryContainer = AppPalete.SuccessGreen,

    error = AppPalete.ErrorRed,
    onError = AppPalete.White,
    errorContainer = AppPalete.ErrorRedBg,
    onErrorContainer = AppPalete. ErrorRed
)