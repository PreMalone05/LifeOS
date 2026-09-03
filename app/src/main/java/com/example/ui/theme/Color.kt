package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Neutral Base & Canvas
val Background = Color.Transparent
val BaseDark = Color(0xFF0F1115)
val SolidSurface = Color(0xFF1E2026) // Solid dark slate for dialogs/pop-ups to eliminate extreme transparency
val OnBackground = Color(0xFFF1F5F9) // Slate-100

val Surface = Color(0x0EFFFFFF) // Semi-transparent glass base
val SurfaceDim = Color(0x0BFFFFFF)
val SurfaceBright = Color(0x1EFFFFFF)
val OnSurface = Color(0xFFF1F5F9)
val OnSurfaceVariant = Color(0xFFCBD5E1) // Slate-300

// Surfaces & Container Levels (Frosted Glass Tonal Stacking)
val SurfaceContainerLowest = Color(0x06FFFFFF)
val SurfaceContainerLow = Color(0x0AFFFFFF)
val SurfaceContainer = Color(0x0FFFFFFF) // 6% white frosted glass
val SurfaceContainerHigh = Color(0x14FFFFFF) // 8% white frosted glass
val SurfaceContainerHighest = Color(0x1AFFFFFF) // 10% white frosted glass
val SurfaceVariant = Color(0x14FFFFFF)

val Outline = Color(0x33FFFFFF) // 20% white subtle outline
val OutlineVariant = Color(0x1AFFFFFF) // 10% white divider

// Semantic Accent & Status Brushes (Frosted Purple / Lavender)
val Primary = Color(0xFFD0BCFF)
val OnPrimary = Color(0xFF381E72)
val PrimaryContainer = Color(0xFF4A4458)
val OnPrimaryContainer = Color(0xFFE8DEF8)

val Secondary = Color(0xFFD0BCFF)
val OnSecondary = Color(0xFF381E72)
val SecondaryContainer = Color(0xFF4A4458)
val OnSecondaryContainer = Color(0xFFE8DEF8)

val Tertiary = Color(0xFF818CF8) // Indigo
val OnTertiary = Color(0xFF1E1B4B)
val TertiaryContainer = Color(0xFF312E81)
val OnTertiaryContainer = Color(0xFFC7D2FE)

val Error = Color(0xFFFCA5A5) // Soft red alert
val OnError = Color(0xFF7F1D1D)
val ErrorContainer = Color(0xFF991B1B)
val OnErrorContainer = Color(0xFFFEE2E2)

val Success = Color(0xFF4ADE80) // Mint green success
val OnSuccess = Color(0xFF052E16)
val SuccessContainer = Color(0xFF14532D)

val Warning = Color(0xFFFFB74D) // Amber warning
val OnWarning = Color(0xFF78350F)
val WarningContainer = Color(0xFFB45309)
val OnWarningContainer = Color(0xFFFEF3C7)

// Fixed Tonal Sets (for gradients and badges)
val PrimaryFixed = Color(0xFFE8DEF8)
val PrimaryFixedDim = Color(0xFFD0BCFF)
val OnPrimaryFixed = Color(0xFF381E72)
val OnPrimaryFixedVariant = Color(0xFF4A4458)

val SecondaryFixed = Color(0xFFE8DEF8)
val SecondaryFixedDim = Color(0xFFD0BCFF)
val OnSecondaryFixed = Color(0xFF381E72)
val OnSecondaryFixedVariant = Color(0xFF4A4458)

val TertiaryFixed = Color(0xFFC7D2FE)
val TertiaryFixedDim = Color(0xFF818CF8)
val OnTertiaryFixed = Color(0xFF1E1B4B)
val OnTertiaryFixedVariant = Color(0xFF312E81)
