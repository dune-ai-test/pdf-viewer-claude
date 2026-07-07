package com.productivity.pdf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.productivity.pdf.ui.theme.ShapePill

private data class AnnotationTool(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

private val tools = listOf(
    AnnotationTool(Icons.Filled.Highlight, "Highlight"),
    AnnotationTool(Icons.Filled.Draw, "Draw"),
    AnnotationTool(Icons.Filled.TextFields, "Text"),
    AnnotationTool(Icons.AutoMirrored.Filled.Chat, "Comment")
)

/**
 * "Annotation Toolbar: A floating pill-shaped container positioned at the bottom
 * or side, using a heavy backdrop blur."
 */
@Composable
fun AnnotationToolbar(modifier: Modifier = Modifier) {
    var selected by remember { mutableIntStateOf(0) }

    Row(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = ShapePill, clip = false)
            .clip(ShapePill)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        tools.forEachIndexed { index, tool ->
            val isSelected = index == selected
            IconButton(
                onClick = { selected = index },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
