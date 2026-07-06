package com.productivity.pdf.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// design.md specifies "Inter" as a highly legible alternative to San Francisco.
// Swap FontFamily.Default for a bundled Inter FontFamily (res/font) if you add the
// Inter .ttf files to the project; the type scale itself mirrors design.md exactly.
private val AppFontFamily = FontFamily.Default

// nav-title: 17/600/22
val NavTitle = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 17.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 22.sp
)

// headline-lg: 34/700/41
val HeadlineLarge = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 34.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 41.sp
)

// headline-lg-mobile: 28/700/34
val HeadlineLargeMobile = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 34.sp
)

// body-main: 17/400/22, letterSpacing -0.4
val BodyMain = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 17.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 22.sp,
    letterSpacing = (-0.4).sp
)

// body-sm: 15/400/20
val BodySmall = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 15.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 20.sp
)

// label-caps: 13/500/18, letterSpacing 0.06
val LabelCaps = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 18.sp,
    letterSpacing = 0.06.sp
)

// caption: 12/400/16
val Caption = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = 12.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 16.sp
)

val AppTypography = Typography(
    headlineLarge = HeadlineLargeMobile, // mobile-first per design.md
    titleLarge = NavTitle,
    titleMedium = NavTitle,
    bodyLarge = BodyMain,
    bodyMedium = BodySmall,
    labelLarge = LabelCaps,
    labelMedium = LabelCaps,
    labelSmall = Caption
)
