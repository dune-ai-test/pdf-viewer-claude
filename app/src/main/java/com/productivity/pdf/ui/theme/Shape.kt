package com.productivity.pdf.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// From design.md `rounded` tokens + Shapes section:
// buttons/inputs -> 10px, cards/modals -> 16px, chips/pills -> full round
val ShapeSmall = RoundedCornerShape(4.dp)      // rounded.sm
val ShapeButton = RoundedCornerShape(10.dp)    // Buttons & Inputs
val ShapeMedium = RoundedCornerShape(12.dp)    // rounded.md
val ShapeCard = RoundedCornerShape(16.dp)      // Cards / Modals
val ShapeXLarge = RoundedCornerShape(24.dp)    // rounded.xl
val ShapePill = RoundedCornerShape(percent = 50) // Selection indicators / toolbars

val AppShapes = Shapes(
    extraSmall = ShapeSmall,
    small = ShapeButton,
    medium = ShapeMedium,
    large = ShapeCard,
    extraLarge = ShapeXLarge
)
