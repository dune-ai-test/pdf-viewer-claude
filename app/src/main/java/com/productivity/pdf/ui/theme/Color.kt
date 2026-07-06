package com.productivity.pdf.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Light scheme (from design.md `colors`) ----
val Surface = Color(0xFFFAF9FE)
val SurfaceDim = Color(0xFFDAD9DF)
val SurfaceBright = Color(0xFFFAF9FE)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF4F3F8)
val SurfaceContainer = Color(0xFFEEEDF3)
val SurfaceContainerHigh = Color(0xFFE9E7ED)
val SurfaceContainerHighest = Color(0xFFE3E2E7)
val OnSurface = Color(0xFF1A1B1F)
val OnSurfaceVariant = Color(0xFF414755)
val InverseSurface = Color(0xFF2F3034)
val InverseOnSurface = Color(0xFFF1F0F5)
val OutlineColor = Color(0xFF717786)
val OutlineVariant = Color(0xFFC1C6D7)
val SurfaceTint = Color(0xFF005BC1)

val Primary = Color(0xFF0058BC) // System Blue
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF0070EB)
val OnPrimaryContainer = Color(0xFFFEFCFF)
val InversePrimary = Color(0xFFADC6FF)

val Secondary = Color(0xFF4C4ACA) // Indigo tint
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF6664E4)
val OnSecondaryContainer = Color(0xFFFFFBFF)

val Tertiary = Color(0xFF9E3D00)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFC64F00)
val OnTertiaryContainer = Color(0xFFFFFBFF)

val ErrorColor = Color(0xFFBA1A1A)
val OnErrorColor = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

val PrimaryFixed = Color(0xFFD8E2FF)
val PrimaryFixedDim = Color(0xFFADC6FF)
val OnPrimaryFixed = Color(0xFF001A41)
val OnPrimaryFixedVariant = Color(0xFF004493)

val SecondaryFixed = Color(0xFFE2DFFF)
val SecondaryFixedDim = Color(0xFFC2C1FF)
val OnSecondaryFixed = Color(0xFF0C006A)
val OnSecondaryFixedVariant = Color(0xFF3631B4)

val Background = Color(0xFFFAF9FE)
val OnBackground = Color(0xFF1A1B1F)
val SurfaceVariant = Color(0xFFE3E2E7)

// ---- Dark scheme derived from design.md inverse/adaptive tokens ----
// "In dark mode, [neutrals] utilize deep charcoals and pure blacks to optimize OLED battery life."
val DarkSurface = Color(0xFF121316)
val DarkSurfaceDim = Color(0xFF121316)
val DarkSurfaceBright = Color(0xFF38393D)
val DarkSurfaceContainerLowest = Color(0xFF0C0D0F)
val DarkSurfaceContainerLow = Color(0xFF1A1B1F)
val DarkSurfaceContainer = Color(0xFF1E1F23)
val DarkSurfaceContainerHigh = Color(0xFF29292E)
val DarkSurfaceContainerHighest = Color(0xFF343439)
val DarkOnSurface = InverseOnSurface
val DarkOnSurfaceVariant = Color(0xFFC1C6D7)
val DarkOutline = Color(0xFF8B909F)
val DarkOutlineVariant = Color(0xFF414755)

val DarkPrimary = InversePrimary
val DarkOnPrimary = OnPrimaryFixedVariant
val DarkPrimaryContainer = OnPrimaryFixedVariant
val DarkOnPrimaryContainer = PrimaryFixed

val DarkSecondary = SecondaryFixedDim
val DarkOnSecondary = OnSecondaryFixedVariant
val DarkSecondaryContainer = OnSecondaryFixedVariant
val DarkOnSecondaryContainer = SecondaryFixed

val DarkTertiary = Color(0xFFFFB595)
val DarkOnTertiary = Color(0xFF5C1A00)
val DarkTertiaryContainer = Color(0xFF7C2E00)
val DarkOnTertiaryContainer = Color(0xFFFFDBCC)
