package pl.edu.ur.blokur.ui.theme

import androidx.compose.ui.graphics.Color

// === Primary: Vibrant Indigo/Violet ===
val Indigo600 = Color(0xFF4F46E5)  // primary buttons, accents
val Indigo700 = Color(0xFF4338CA)  // pressed states
val Indigo50 = Color(0xFFEEF2FF)  // primary container (ultra-light)
val Indigo100 = Color(0xFFE0E7FF)  // subtle tinted surfaces

// === Secondary: Warm Amber ===
val Amber500 = Color(0xFFF59E0B)
val Amber600 = Color(0xFFD97706)
val Amber50 = Color(0xFFFFFBEB)
val Amber100 = Color(0xFFFEF3C7)

// === Neutral backgrounds (warm white, not cold) ===
val White = Color(0xFFFFFFFF)
val NeutralBg = Color(0xFFF8F9FF)   // page background — very slight indigo tint
val NeutralSurface = Color(0xFFFFFFFF)  // cards
val NeutralSurface2 = Color(0xFFF3F4FF) // secondary surfaces
val Stroke = Color(0xFFE5E7F0)   // card borders
val StrokeLight = Color(0xFFEEEFF8)   // subtle dividers

// === Text ===
val TextPrimary = Color(0xFF111827)   // near-black — high contrast
val TextSecondary = Color(0xFF6B7280)   // medium gray
val TextMuted = Color(0xFF9CA3AF)   // hints, labels

// === Semantic ===
val SuccessGreen = Color(0xFF059669)
val SuccessGreenBg = Color(0xFFECFDF5)
val WarningOrange = Color(0xFFD97706)
val WarningOrangeBg = Color(0xFFFFFBEB)
val ErrorRed = Color(0xFFDC2626)
val ErrorRedBg = Color(0xFFFEF2F2)
val InfoBlue = Color(0xFF2563EB)

// === Gradient helpers ===
val GradientStart = Indigo600
val GradientEnd = Color(0xFF7C3AED)  // violet
val GradientAmber = Amber500